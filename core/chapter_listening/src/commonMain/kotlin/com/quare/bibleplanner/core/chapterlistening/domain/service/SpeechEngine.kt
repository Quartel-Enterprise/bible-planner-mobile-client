package com.quare.bibleplanner.core.chapterlistening.domain.service

import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechEngineEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechUtteranceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechVoicesModel
import kotlinx.coroutines.flow.Flow

interface SpeechEngine {
    val isSupported: Boolean
    val events: Flow<SpeechEngineEvent>

    suspend fun loadVoices(languageTag: String): SpeechVoicesModel

    fun speak(
        utterances: List<SpeechUtteranceModel>,
        voiceId: String?,
        languageTag: String,
        speed: Float,
    )

    fun stop()

    fun openVoiceSettings()
}
