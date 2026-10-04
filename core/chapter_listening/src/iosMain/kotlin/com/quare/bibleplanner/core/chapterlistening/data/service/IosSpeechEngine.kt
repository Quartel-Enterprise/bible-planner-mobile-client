package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVoiceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechEngineEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechUtteranceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechVoicesModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.SpeechEngine
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesisVoiceQualityDefault
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechUtteranceDefaultSpeechRate
import platform.AVFAudio.AVSpeechUtteranceMaximumSpeechRate
import platform.AVFAudio.AVSpeechUtteranceMinimumSpeechRate
import platform.Foundation.NSRange
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject
import kotlin.time.DurationUnit

@OptIn(ExperimentalForeignApi::class)
internal class IosSpeechEngine : SpeechEngine {
    override val isSupported: Boolean = true

    private val eventChannel = Channel<SpeechEngineEvent>(Channel.UNLIMITED)
    override val events: Flow<SpeechEngineEvent> = eventChannel.receiveAsFlow()

    private val synthesizer = AVSpeechSynthesizer()
    private val utteranceIds = mutableMapOf<AVSpeechUtterance, String>()

    // Why: the synthesizer only keeps a weak reference to its delegate.
    private val delegate = SynthesizerDelegate()

    init {
        synthesizer.delegate = delegate
        synthesizer.usesApplicationAudioSession = true
    }

    override suspend fun loadVoices(languageTag: String): SpeechVoicesModel {
        val language = languageTag.substringBefore(LANGUAGE_SEPARATOR)
        val voices = AVSpeechSynthesisVoice
            .speechVoices()
            .filterIsInstance<AVSpeechSynthesisVoice>()
            .filter { voice ->
                voice.language.substringBefore(LANGUAGE_SEPARATOR) == language && voice.isReadingVoice()
            }.sortedWith(
                compareByDescending<AVSpeechSynthesisVoice> { voice -> voice.language == languageTag }
                    .thenByDescending { voice -> voice.quality }
                    .thenBy { voice -> voice.name },
            )
        if (voices.isEmpty()) return SpeechVoicesModel.Unavailable
        val models = voices.map { voice ->
            ListeningVoiceModel(
                id = voice.identifier,
                name = voice.name,
                languageTag = voice.language,
                isEnhanced = voice.quality != AVSpeechSynthesisVoiceQualityDefault,
            )
        }
        return SpeechVoicesModel.Available(
            voices = models,
            shouldSuggestEnhancedVoice = models.none(ListeningVoiceModel::isEnhanced),
        )
    }

    override fun speak(
        utterances: List<SpeechUtteranceModel>,
        voiceId: String?,
        languageTag: String,
        speed: Float,
    ) {
        stop()
        val voice = voiceId?.let(AVSpeechSynthesisVoice::voiceWithIdentifier)
            ?: AVSpeechSynthesisVoice.voiceWithLanguage(languageTag)
        val rate = (AVSpeechUtteranceDefaultSpeechRate * speed).coerceIn(
            AVSpeechUtteranceMinimumSpeechRate,
            AVSpeechUtteranceMaximumSpeechRate,
        )
        utterances.forEach { model ->
            val utterance = AVSpeechUtterance.speechUtteranceWithString(model.text)
            utterance.voice = voice
            utterance.rate = rate
            utterance.preUtteranceDelay = model.leadingSilence.toDouble(DurationUnit.SECONDS)
            utteranceIds[utterance] = model.id
            synthesizer.speakUtterance(utterance)
        }
    }

    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }

    override fun openVoiceSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(
            url = url,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }

    // Why: novelty (Bells, Bubbles…) and Eloquence voices are listed for every language but read scripture badly.
    private fun AVSpeechSynthesisVoice.isReadingVoice(): Boolean =
        NOVELTY_VOICE_MARKER !in identifier && ELOQUENCE_VOICE_MARKER !in identifier

    private fun send(
        utterance: AVSpeechUtterance,
        isLast: Boolean,
        createEvent: (String) -> SpeechEngineEvent,
    ) {
        val id = (if (isLast) utteranceIds.remove(utterance) else utteranceIds[utterance]) ?: return
        eventChannel.trySend(createEvent(id))
    }

    private inner class SynthesizerDelegate :
        NSObject(),
        AVSpeechSynthesizerDelegateProtocol {
        @ObjCSignatureOverride
        override fun speechSynthesizer(
            synthesizer: AVSpeechSynthesizer,
            didStartSpeechUtterance: AVSpeechUtterance,
        ) {
            send(
                utterance = didStartSpeechUtterance,
                isLast = false,
                createEvent = SpeechEngineEvent::Started,
            )
        }

        override fun speechSynthesizer(
            synthesizer: AVSpeechSynthesizer,
            willSpeakRangeOfSpeechString: CValue<NSRange>,
            utterance: AVSpeechUtterance,
        ) {
            val charIndex = willSpeakRangeOfSpeechString.useContents { location.toInt() }
            send(
                utterance = utterance,
                isLast = false,
                createEvent = { id ->
                    SpeechEngineEvent.RangeStarted(
                        utteranceId = id,
                        charIndex = charIndex,
                    )
                },
            )
        }

        @ObjCSignatureOverride
        override fun speechSynthesizer(
            synthesizer: AVSpeechSynthesizer,
            didFinishSpeechUtterance: AVSpeechUtterance,
        ) {
            send(
                utterance = didFinishSpeechUtterance,
                isLast = true,
                createEvent = SpeechEngineEvent::Finished,
            )
        }

        // Why: a stopped utterance never finishes, and a voice preview still has to end.
        @ObjCSignatureOverride
        override fun speechSynthesizer(
            synthesizer: AVSpeechSynthesizer,
            didCancelSpeechUtterance: AVSpeechUtterance,
        ) {
            send(
                utterance = didCancelSpeechUtterance,
                isLast = true,
                createEvent = SpeechEngineEvent::Finished,
            )
        }
    }

    private companion object {
        const val LANGUAGE_SEPARATOR = '-'
        const val NOVELTY_VOICE_MARKER = "speech.synthesis"
        const val ELOQUENCE_VOICE_MARKER = "eloquence"
    }
}
