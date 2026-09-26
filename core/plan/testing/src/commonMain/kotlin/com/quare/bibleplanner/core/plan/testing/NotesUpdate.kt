package com.quare.bibleplanner.core.plan.testing

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

data class NotesUpdate(
    val weekNumber: Int,
    val dayNumber: Int,
    val readingPlanType: ReadingPlanType,
    val notes: String?,
)
