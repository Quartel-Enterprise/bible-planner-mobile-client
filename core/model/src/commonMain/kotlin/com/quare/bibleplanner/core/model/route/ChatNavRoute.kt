package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class ChatNavRoute(
    val source: ChatEntrySource,
    val dayNumber: Int?,
    val weekNumber: Int?,
    val readingPlanType: String?,
) : NavRoute

fun ChatNavRoute.toDayNavRoute(): DayNavRoute? {
    val day = dayNumber ?: return null
    val week = weekNumber ?: return null
    val planType = readingPlanType ?: return null
    return DayNavRoute(
        dayNumber = day,
        weekNumber = week,
        readingPlanType = planType,
    )
}
