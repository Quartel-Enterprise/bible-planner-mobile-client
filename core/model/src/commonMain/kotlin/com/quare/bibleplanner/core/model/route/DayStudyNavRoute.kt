package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class DayStudyNavRoute(
    val dayNumber: Int,
    val weekNumber: Int,
    val readingPlanType: String,
) : NavRoute

fun DayStudyNavRoute.toDayNavRoute(): DayNavRoute = DayNavRoute(
    dayNumber = dayNumber,
    weekNumber = weekNumber,
    readingPlanType = readingPlanType,
)
