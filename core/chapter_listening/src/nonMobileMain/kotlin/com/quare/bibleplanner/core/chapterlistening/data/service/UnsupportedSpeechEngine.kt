package com.quare.bibleplanner.core.chapterlistening.data.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechEngineEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechUtteranceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechVoicesModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.SpeechEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class UnsupportedSpeechEngine : SpeechEngine {
    override val isSupported: Boolean = false
    override val events: Flow<SpeechEngineEvent> = emptyFlow()

    override suspend fun loadVoices(languageTag: String): SpeechVoicesModel = SpeechVoicesModel.Unavailable

    override fun speak(
        utterances: List<SpeechUtteranceModel>,
        voiceId: String?,
        languageTag: String,
        speed: Float,
    ) {
    }

    override fun stop() {
    }

    override fun openVoiceSettings() {
    }
}
