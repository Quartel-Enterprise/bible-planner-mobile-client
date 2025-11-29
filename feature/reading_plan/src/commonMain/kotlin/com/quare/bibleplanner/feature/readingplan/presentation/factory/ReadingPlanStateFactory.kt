package com.quare.bibleplanner.feature.readingplan.presentation.factory

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState

internal class ReadingPlanStateFactory {
    private val defaultPlan = ReadingPlanType.CHRONOLOGICAL

    fun createLoading(): ReadingPlanUiState.Loading = ReadingPlanUiState.Loading(
        selectedReadingPlan = defaultPlan,
    )
}
