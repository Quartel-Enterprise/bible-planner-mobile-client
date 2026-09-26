package com.quare.bibleplanner.core.daystudy.domain.model

sealed interface DayStudyGenerationEventModel {
    data class PhaseChanged(
        val phase: DayStudyPhaseModel,
    ) : DayStudyGenerationEventModel

    data class Completed(
        val study: DayStudyModel,
    ) : DayStudyGenerationEventModel
}
