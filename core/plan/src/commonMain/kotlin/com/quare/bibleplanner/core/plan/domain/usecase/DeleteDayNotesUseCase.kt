package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

class DeleteDayNotesUseCase(
    private val updateDayNotes: UpdateDayNotesUseCase,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: ReadingPlanType,
    ) {
        updateDayNotes(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            readingPlanType = readingPlanType,
            notes = null,
        )
    }
}
