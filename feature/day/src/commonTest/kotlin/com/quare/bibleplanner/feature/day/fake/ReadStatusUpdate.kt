package com.quare.bibleplanner.feature.day.fake

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

internal data class ReadStatusUpdate(
    val weekNumber: Int,
    val dayNumber: Int,
    val readingPlanType: ReadingPlanType,
    val isRead: Boolean,
    val readTimestamp: Long?,
)
