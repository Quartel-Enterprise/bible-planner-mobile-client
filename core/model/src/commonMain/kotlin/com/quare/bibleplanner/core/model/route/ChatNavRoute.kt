package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

/**
 * The AI chat screen. The day triple is the reading the conversation starts with; all three are
 * null when the chat is opened without a reading context (the user can still ask anything). New
 * contexts are added as new nullable groups here, mirroring the server's `context_type`.
 */
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
