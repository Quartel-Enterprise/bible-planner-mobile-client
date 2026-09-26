package com.quare.bibleplanner.feature.day.fake

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeDayRepository(
    private val daysWithNotesCount: Int,
    day: DayModel?,
) : DayRepository {
    val day = MutableStateFlow(day)
    val readStatusUpdates = mutableListOf<ReadStatusUpdate>()
    val notesUpdates = mutableListOf<String?>()

    override fun getDayByWeekAndDayFlow(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): Flow<DayModel?> = day

    override suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ): DayModel? = day.value

    override suspend fun updateDayReadStatus(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        isRead: Boolean,
        readTimestamp: Long?,
    ) {
        readStatusUpdates += ReadStatusUpdate(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            readingPlanType = readingPlanType,
            isRead = isRead,
            readTimestamp = readTimestamp,
        )
    }

    override suspend fun updateDayNotes(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        notes: String?,
    ) {
        notesUpdates += notes
        day.value = day.value?.copy(notes = notes)
    }

    override suspend fun getDaysWithNotesCount(): Int = daysWithNotesCount
}
