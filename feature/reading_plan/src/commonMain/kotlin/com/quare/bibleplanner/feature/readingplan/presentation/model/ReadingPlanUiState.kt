package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

internal sealed interface ReadingPlanUiState {
    val selectedReadingPlan: ReadingPlanType

    data class Loaded(
        val weekPlans: List<WeekPlanPresentationModel>,
        val progress: Float,
        override val selectedReadingPlan: ReadingPlanType,
    ) : ReadingPlanUiState

    data class Loading(
        override val selectedReadingPlan: ReadingPlanType,
    ) : ReadingPlanUiState
}
