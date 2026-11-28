package com.quare.bibleplanner.feature.daystudy.presentation.model

sealed interface DayStudyRouteUiAction {
    data object ShowLimitReached : DayStudyRouteUiAction

    data class ShowWaitForGenerations(
        val count: Int,
    ) : DayStudyRouteUiAction
}
