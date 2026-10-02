package com.quare.bibleplanner.core.chapterstudy.domain.model

sealed interface ChapterStudyGenerationEventModel {
    data class PhaseChanged(
        val phase: ChapterStudyPhaseModel,
    ) : ChapterStudyGenerationEventModel

    data class Completed(
        val study: ChapterStudyModel,
    ) : ChapterStudyGenerationEventModel
}
