package com.quare.bibleplanner.core.chapterlistening.domain.model

sealed interface ListeningWordTargetModel {
    data object ChapterBook : ListeningWordTargetModel

    data object ChapterNumber : ListeningWordTargetModel

    data class Heading(
        val range: IntRange,
    ) : ListeningWordTargetModel

    data class Verse(
        val range: IntRange,
    ) : ListeningWordTargetModel
}
