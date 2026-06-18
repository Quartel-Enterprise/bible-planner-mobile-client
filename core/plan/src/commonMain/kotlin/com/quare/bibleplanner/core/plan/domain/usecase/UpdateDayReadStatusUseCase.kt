package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.UpdatePassageReadStatusUseCase
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository
import kotlinx.coroutines.flow.first

class UpdateDayReadStatusUseCase(
    private val dayRepository: DayRepository,
    private val updatePassageReadStatus: UpdatePassageReadStatusUseCase,
    private val getPlansByWeekUseCase: GetPlansByWeekUseCase,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        isRead: Boolean,
        readingPlanType: ReadingPlanType,
    ) {
        // Get the day to access its passages
        val plansModel = getPlansByWeekUseCase().first()
        val weeks = when (readingPlanType) {
            ReadingPlanType.CHRONOLOGICAL -> plansModel.chronologicalOrder
            ReadingPlanType.BOOKS -> plansModel.booksOrder
        }
        val week = weeks.find { it.number == weekNumber } ?: return
        val day = week.days.find { it.number == dayNumber } ?: return

        // Update all passages
        updatePassageReadStatus(day.passages)

        // Update day read status with timestamp
        val readTimestamp = if (isRead) {
            currentTimestampProvider.getCurrentTimestamp()
        } else {
            null
        }
        dayRepository.updateDayReadStatus(weekNumber, dayNumber, readingPlanType, isRead, readTimestamp)
    }
}
