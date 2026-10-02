package com.quare.bibleplanner.core.chapterstudy.domain.model

data class ChapterStudyGenerationJob(
    val target: ChapterStudyTargetModel,
    val phase: ChapterStudyPhaseModel?,
    val status: ChapterStudyGenerationStatus,
)
