package com.quare.bibleplanner.core.plan.testing

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

data class ReadStatusUpdate(
    val weekNumber: Int,
    val dayNumber: Int,
    val readingPlanType: ReadingPlanType,
    val isRead: Boolean,
    val readTimestamp: Long?,
)
