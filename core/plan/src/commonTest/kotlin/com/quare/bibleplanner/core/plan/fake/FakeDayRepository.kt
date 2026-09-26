package com.quare.bibleplanner.core.plan.fake

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository
import kotlinx.coroutines.flow.Flow

internal class FakeDayRepository(
    private val daysWithNotesCount: Int = 0,
) : DayRepository {
    val calls = mutableListOf<String>()

    override fun getDayByWeekAndDayFlow(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): Flow<DayModel?> = error("Unexpected call")

    override suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): DayModel? = error("Unexpected call")

    override suspend fun updateDayReadStatus(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        isRead: Boolean,
        readTimestamp: Long?,
    ) {
        calls += "updateDayReadStatus($weekNumber, $dayNumber, $readingPlanType, $isRead, $readTimestamp)"
    }

    override suspend fun updateDayNotes(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        notes: String?,
    ) {
        calls += "updateDayNotes($weekNumber, $dayNumber, $readingPlanType, $notes)"
    }

    override suspend fun getDaysWithNotesCount(): Int = daysWithNotesCount
}
