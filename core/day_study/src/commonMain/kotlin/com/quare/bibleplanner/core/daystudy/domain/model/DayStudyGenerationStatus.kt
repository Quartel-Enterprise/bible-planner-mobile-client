package com.quare.bibleplanner.core.daystudy.domain.model

sealed interface DayStudyGenerationStatus {
    data object Generating : DayStudyGenerationStatus

    data class Done(
        val study: DayStudyModel,
    ) : DayStudyGenerationStatus

    data class Failed(
        val isLimitReached: Boolean,
        val isOffline: Boolean,
    ) : DayStudyGenerationStatus
}
