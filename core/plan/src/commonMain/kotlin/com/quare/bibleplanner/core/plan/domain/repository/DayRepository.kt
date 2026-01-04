package com.quare.bibleplanner.core.plan.domain.repository

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlinx.coroutines.flow.Flow

interface DayRepository {
    fun getDayByWeekAndDayFlow(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): Flow<DayModel?>

    suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): DayModel?

    suspend fun updateDayReadStatus(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        isRead: Boolean,
        readTimestamp: Long?,
    )

    suspend fun updateDayNotes(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        notes: String?,
    )

    suspend fun getDaysWithNotesCount(): Int
}
