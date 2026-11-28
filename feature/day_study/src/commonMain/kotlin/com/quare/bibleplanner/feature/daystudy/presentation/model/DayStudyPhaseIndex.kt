package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyPhaseModel

internal fun DayStudyPhaseModel.toPhaseIndex(): Int = when (this) {
    DayStudyPhaseModel.READING -> DayStudyGenerationPhase.READING.ordinal
    DayStudyPhaseModel.CHAPTERS -> DayStudyGenerationPhase.CHAPTERS.ordinal
    DayStudyPhaseModel.CONTEXT -> DayStudyGenerationPhase.CONTEXT.ordinal
    DayStudyPhaseModel.QUESTIONS -> DayStudyGenerationPhase.QUESTIONS.ordinal
}
