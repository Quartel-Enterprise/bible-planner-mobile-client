package com.quare.bibleplanner.feature.daystudy.presentation.model

internal sealed interface DayStudyUiAction {
    data object NavigateToStudy : DayStudyUiAction

    data object NavigateToPaywall : DayStudyUiAction

    data object NavigateToLoginWarning : DayStudyUiAction

    data object ShowOffline : DayStudyUiAction

    data class ShowWaitForGenerations(
        val count: Int,
    ) : DayStudyUiAction
}
