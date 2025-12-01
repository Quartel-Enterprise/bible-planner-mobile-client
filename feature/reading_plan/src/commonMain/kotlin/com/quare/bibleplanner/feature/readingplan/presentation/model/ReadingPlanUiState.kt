package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

internal sealed interface ReadingPlanUiState {
    val selectedReadingPlan: ReadingPlanType
    val isShowingMenu: Boolean

    data class Loaded(
        val weekPlans: List<WeekPlanPresentationModel>,
        val progress: Float,
        override val selectedReadingPlan: ReadingPlanType,
        override val isShowingMenu: Boolean,
    ) : ReadingPlanUiState

    data class Loading(
        override val selectedReadingPlan: ReadingPlanType,
        override val isShowingMenu: Boolean,
    ) : ReadingPlanUiState
}
