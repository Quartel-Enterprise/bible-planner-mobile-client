package com.quare.bibleplanner.core.chapterstudy.domain.model

sealed interface ChapterStudyGenerationStatus {
    data object Generating : ChapterStudyGenerationStatus

    data class Done(
        val study: ChapterStudyModel,
    ) : ChapterStudyGenerationStatus

    data class Failed(
        val isLimitReached: Boolean,
        val isOffline: Boolean,
    ) : ChapterStudyGenerationStatus
}
