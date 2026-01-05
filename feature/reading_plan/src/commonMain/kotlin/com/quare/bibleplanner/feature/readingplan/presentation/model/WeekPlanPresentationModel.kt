package com.quare.bibleplanner.feature.readingplan.presentation.model

import com.quare.bibleplanner.core.model.plan.WeekPlanModel

internal data class WeekPlanPresentationModel(
    val weekPlan: WeekPlanModel,
    val dayPlans: List<DayPlanPresentationModel>,
    val isExpanded: Boolean,
    val readDaysCount: Int,
    val totalDays: Int,
)
