package com.quare.bibleplanner.feature.daystudy.presentation.model

internal data class DayStudyGenerationUiModel(
    val currentPhaseIndex: Int,
) {
    val activePhase: DayStudyGenerationPhase
        get() = DayStudyGenerationPhase.entries[
            currentPhaseIndex.coerceAtMost(DayStudyGenerationPhase.entries.lastIndex),
        ]

    fun isDone(phase: DayStudyGenerationPhase): Boolean = phase.ordinal < currentPhaseIndex

    fun isActive(phase: DayStudyGenerationPhase): Boolean = phase.ordinal == currentPhaseIndex
}
