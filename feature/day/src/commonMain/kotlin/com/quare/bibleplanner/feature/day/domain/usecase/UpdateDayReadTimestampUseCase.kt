package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.feature.day.domain.repository.DayRepository

class UpdateDayReadTimestampUseCase(
    private val dayRepository: DayRepository,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        readTimestamp: Long, // Epoch milliseconds
    ) {
        dayRepository.updateDayReadStatus(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            isRead = true,
            readTimestamp = readTimestamp,
        )
    }
}
