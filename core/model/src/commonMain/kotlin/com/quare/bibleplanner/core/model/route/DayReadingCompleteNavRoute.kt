package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class DayReadingCompleteNavRoute(
    val dayNumber: Int,
    val weekNumber: Int,
    val readingPlanType: String,
) : NavRoute

fun DayReadingCompleteNavRoute.toDayNavRoute(): DayNavRoute = DayNavRoute(
    dayNumber = dayNumber,
    weekNumber = weekNumber,
    readingPlanType = readingPlanType,
)
