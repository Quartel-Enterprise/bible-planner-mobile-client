package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

internal sealed interface ReadingPlanUiState {
    val selectedReadingPlan: ReadingPlanType
    val isShowingMenu: Boolean
    val scrollToWeekNumber: Int
    val scrollToTop: Boolean
    val isScrolledDown: Boolean

    data class Loaded(
        val weekPlans: List<WeekPlanPresentationModel>,
        val progress: Float,
        override val selectedReadingPlan: ReadingPlanType,
        override val isShowingMenu: Boolean,
        override val scrollToWeekNumber: Int = 0,
        override val scrollToTop: Boolean = false,
        override val isScrolledDown: Boolean = false,
    ) : ReadingPlanUiState

    data class Loading(
        override val selectedReadingPlan: ReadingPlanType,
        override val isShowingMenu: Boolean,
        override val scrollToWeekNumber: Int = 0,
        override val scrollToTop: Boolean = false,
        override val isScrolledDown: Boolean = false,
    ) : ReadingPlanUiState
}
