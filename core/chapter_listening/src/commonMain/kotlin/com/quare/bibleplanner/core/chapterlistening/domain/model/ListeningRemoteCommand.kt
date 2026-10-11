package com.quare.bibleplanner.core.chapterlistening.domain.model

import kotlin.time.Duration

sealed interface ListeningRemoteCommand {
    data object Play : ListeningRemoteCommand

    data object Pause : ListeningRemoteCommand

    data object Stop : ListeningRemoteCommand

    data object NextVerse : ListeningRemoteCommand

    data object PreviousVerse : ListeningRemoteCommand

    data class SkipToVerse(
        val verseIndex: Int,
    ) : ListeningRemoteCommand

    data class SeekTo(
        val position: Duration,
    ) : ListeningRemoteCommand

    data object NextChapter : ListeningRemoteCommand

    data object PreviousChapter : ListeningRemoteCommand
}
