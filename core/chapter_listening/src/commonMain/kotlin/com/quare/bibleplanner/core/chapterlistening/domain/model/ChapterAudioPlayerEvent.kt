package com.quare.bibleplanner.core.chapterlistening.domain.model

sealed interface ChapterAudioPlayerEvent {
    val playbackId: Int

    data class Ended(
        override val playbackId: Int,
    ) : ChapterAudioPlayerEvent

    data class Failed(
        override val playbackId: Int,
    ) : ChapterAudioPlayerEvent
}
