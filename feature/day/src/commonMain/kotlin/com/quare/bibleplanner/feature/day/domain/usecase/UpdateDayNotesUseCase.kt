package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.repository.DayRepository

class UpdateDayNotesUseCase(
    private val dayRepository: DayRepository,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
        notes: String?,
    ) {
        dayRepository.updateDayNotes(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            readingPlanType = readingPlanType,
            notes = notes,
        )
    }
}
