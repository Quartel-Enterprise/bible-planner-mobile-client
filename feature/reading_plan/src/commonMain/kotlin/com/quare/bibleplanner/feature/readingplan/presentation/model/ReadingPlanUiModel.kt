package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

internal data class ReadingPlanUiModel(
    val selectedReadingPlan: ReadingPlanType,
    val weeks: List<String>,
    val progress: Float,
)
