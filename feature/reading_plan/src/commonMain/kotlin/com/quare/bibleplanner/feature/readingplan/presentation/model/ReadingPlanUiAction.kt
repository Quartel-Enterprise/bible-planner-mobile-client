package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

sealed interface ReadingPlanUiAction {
    data class GoToDay(
        val dayNumber: Int,
        val weekNumber: Int,
        val readingPlanType: ReadingPlanType,
    ) : ReadingPlanUiAction

    data object GoToDeleteAllProgress : ReadingPlanUiAction

    data object GoToChangeStartDate : ReadingPlanUiAction

    data object GoToOnboarding : ReadingPlanUiAction

    data object ShowNoProgressToDelete : ReadingPlanUiAction

    data object GoToTheme : ReadingPlanUiAction

    data class OpenLink(
        val url: String,
    ) : ReadingPlanUiAction
}
