package com.quare.bibleplanner.feature.readingplan.presentation.factory

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState

internal class ReadingPlanStateFactory {
    fun createLoaded(): ReadingPlanUiState.Loaded = ReadingPlanUiState.Loaded(
        ReadingPlanUiModel(
            selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
            weeks = emptyList(),
            progress = 0f,
        )
    )
}
