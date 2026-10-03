package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository

class UpdateDayReadTimestampUseCase(
    private val dayRepository: DayRepository,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        // Why: epoch milliseconds, the unit DayEntity.readTimestamp stores.
        readTimestamp: Long,
    ) {
        dayRepository.updateDayReadStatus(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            readingPlanType = readingPlanType,
            isRead = true,
            readTimestamp = readTimestamp,
        )
    }
}
