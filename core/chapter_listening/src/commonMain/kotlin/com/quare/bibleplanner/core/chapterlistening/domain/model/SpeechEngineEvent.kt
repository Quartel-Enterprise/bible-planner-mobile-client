package com.quare.bibleplanner.core.chapterlistening.domain.model

sealed interface SpeechEngineEvent {
    val utteranceId: String

    data class Started(
        override val utteranceId: String,
    ) : SpeechEngineEvent

    data class RangeStarted(
        override val utteranceId: String,
        val charIndex: Int,
    ) : SpeechEngineEvent

    data class Finished(
        override val utteranceId: String,
    ) : SpeechEngineEvent

    data class Failed(
        override val utteranceId: String,
    ) : SpeechEngineEvent
}
