package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.feature.readingplan.domain.model.ReadingPlanType

internal data class ReadingPlanUiState(
    val selectedReadingPlan: ReadingPlanType,
    val weeks: List<String>,
    val progress: Float,
)
