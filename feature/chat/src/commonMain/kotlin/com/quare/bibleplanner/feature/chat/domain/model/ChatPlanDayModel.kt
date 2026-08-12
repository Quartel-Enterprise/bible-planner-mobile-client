package com.quare.bibleplanner.feature.chat.domain.model

/**
 * Which day of which plan a conversation was started from. It is what tells the day's own thread
 * apart from every other one, since passages cannot: two plans can schedule the same chapters.
 */
data class ChatPlanDayModel(
    val dayNumber: Int,
    val weekNumber: Int,
    val readingPlanType: String,
)
