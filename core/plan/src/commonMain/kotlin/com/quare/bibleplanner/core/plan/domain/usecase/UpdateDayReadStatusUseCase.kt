package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.MarkPassagesReadUseCase
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class UpdateDayReadStatusUseCase(
    private val dayRepository: DayRepository,
    private val markPassagesReadUseCase: MarkPassagesReadUseCase,
    private val getPlansByWeekUseCase: GetPlansByWeekUseCase,
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
        markPassagesReadUseCase(day.passages)

        // Update day read status with timestamp
        val readTimestamp = if (isRead) {
            Clock.System.now().toEpochMilliseconds()
        } else {
            null
        }
        dayRepository.updateDayReadStatus(weekNumber, dayNumber, isRead, readTimestamp)
    }
}
