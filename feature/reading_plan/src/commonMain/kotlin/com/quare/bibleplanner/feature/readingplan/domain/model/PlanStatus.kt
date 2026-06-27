package com.quare.bibleplanner.feature.readingplan.domain.model

internal data class PlanStatus(
    val mode: PlanMode,
    val nextDay: PlanDayRef?,
    val todayDay: PlanDayRef?,
    val totalDays: Int,
    val readDays: Int,
    val streakDays: Int,
    val daysAhead: Int,
    val daysBehind: Int,
    val daysSinceLastRead: Int?,
)
