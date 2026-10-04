package com.quare.bibleplanner.core.chapterlistening.data.service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVoiceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechEngineEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechUtteranceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechVoicesModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.SpeechEngine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class AndroidSpeechEngine(
    private val context: Context,
) : SpeechEngine {
    override val isSupported: Boolean = true

    private val eventChannel = Channel<SpeechEngineEvent>(Channel.UNLIMITED)
    override val events: Flow<SpeechEngineEvent> = eventChannel.receiveAsFlow()

    private val initializationTimeout: Duration = 10.seconds
    private val initialization = CompletableDeferred<Boolean>()

    // Why: the controller caches voices per language and skips reloading them, so keep every language's voices.
    private var installedVoices: Map<String, Voice> = emptyMap()

    private val textToSpeech: TextToSpeech by lazy {
        TextToSpeech(context) { status -> initialization.complete(status == TextToSpeech.SUCCESS) }.apply {
            setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            setOnUtteranceProgressListener(ProgressListener())
        }
    }

    override suspend fun loadVoices(languageTag: String): SpeechVoicesModel {
        val engine = textToSpeech
        val isReady = withTimeoutOrNull(initializationTimeout) { initialization.await() } == true
        if (!isReady) return SpeechVoicesModel.Unavailable
        val locale = Locale.forLanguageTag(languageTag)
        val voices = withContext(Dispatchers.IO) { engine.voices.orEmpty() }
            .filter { voice -> voice.locale.language == locale.language && voice.isUsableOffline() }
            .sortedWith(
                compareByDescending<Voice> { voice -> voice.locale.country == locale.country }
                    .thenByDescending(Voice::getQuality)
                    .thenBy(Voice::getName),
            )
        installedVoices = installedVoices + voices.associateBy(Voice::getName)
        if (voices.isEmpty()) return SpeechVoicesModel.Unavailable
        return SpeechVoicesModel.Available(
            voices = voices.map { voice ->
                ListeningVoiceModel(
                    id = voice.name,
                    name = "",
                    languageTag = voice.locale.toLanguageTag(),
                    isEnhanced = voice.quality >= Voice.QUALITY_HIGH,
                )
            },
            shouldSuggestEnhancedVoice = false,
        )
    }

    /*
     * Why: the first utterance flushes the queue, so a new call always replaces what was still
     * waiting to be spoken instead of reading it after.
     */
    override fun speak(
        utterances: List<SpeechUtteranceModel>,
        voiceId: String?,
        languageTag: String,
        speed: Float,
    ) {
        val engine = textToSpeech
        engine.setSpeechRate(speed)
        val voice = voiceId?.let(installedVoices::get)
        if (voice == null) {
            engine.language = Locale.forLanguageTag(languageTag)
        } else {
            engine.voice = voice
        }
        utterances.forEachIndexed { index, utterance ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            if (utterance.leadingSilence > Duration.ZERO) {
                engine.playSilentUtterance(
                    utterance.leadingSilence.inWholeMilliseconds,
                    queueMode,
                    "${utterance.id}$SILENCE_SUFFIX",
                )
            }
            val textQueueMode = if (utterance.leadingSilence > Duration.ZERO) TextToSpeech.QUEUE_ADD else queueMode
            engine.speak(utterance.text, textQueueMode, null, utterance.id)
        }
    }

    override fun stop() {
        if (initialization.isCompleted) textToSpeech.stop()
    }

    override fun openVoiceSettings() {
        val ttsSettings = Intent(TTS_SETTINGS_ACTION).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(ttsSettings)
        } catch (exception: ActivityNotFoundException) {
            Logger.w(exception) { "No text-to-speech settings screen, opening the system settings" }
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun Voice.isUsableOffline(): Boolean =
        !isNetworkConnectionRequired && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in features.orEmpty()

    private fun send(event: SpeechEngineEvent) {
        if (event.utteranceId.endsWith(SILENCE_SUFFIX)) return
        eventChannel.trySend(event)
    }

    private inner class ProgressListener : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) {
            send(SpeechEngineEvent.Started(utteranceId))
        }

        override fun onRangeStart(
            utteranceId: String,
            start: Int,
            end: Int,
            frame: Int,
        ) {
            send(
                SpeechEngineEvent.RangeStarted(
                    utteranceId = utteranceId,
                    charIndex = start,
                ),
            )
        }

        override fun onDone(utteranceId: String) {
            send(SpeechEngineEvent.Finished(utteranceId))
        }

        // Why: a flushed utterance never reaches onDone, and a voice preview still has to end.
        override fun onStop(
            utteranceId: String,
            interrupted: Boolean,
        ) {
            send(SpeechEngineEvent.Finished(utteranceId))
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String) {
            send(SpeechEngineEvent.Failed(utteranceId))
        }

        override fun onError(
            utteranceId: String,
            errorCode: Int,
        ) {
            send(SpeechEngineEvent.Failed(utteranceId))
        }
    }

    private companion object {
        const val SILENCE_SUFFIX = "-silence"
        const val TTS_SETTINGS_ACTION = "com.android.settings.TTS_SETTINGS"
    }
}
