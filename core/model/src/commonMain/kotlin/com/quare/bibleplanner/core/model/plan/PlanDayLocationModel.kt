package com.quare.bibleplanner.core.model.plan

data class PlanDayLocationModel(
    val weekNumber: Int,
    val dayNumber: Int,
    val readingPlanType: ReadingPlanType,
)
