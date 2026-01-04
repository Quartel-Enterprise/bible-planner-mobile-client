package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.plan.ReadingPlanType

class ReadDayToggleOperationUseCase(
    private val updateDayReadStatus: UpdateDayReadStatusUseCase,
) {
    suspend operator fun invoke(
        newReadStatus: Boolean,
        weekNumber: Int,
        dayNumber: Int,
        selectedReadingPlan: ReadingPlanType,
    ) {
        updateDayReadStatus(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            isRead = newReadStatus,
            readingPlanType = selectedReadingPlan,
        )
    }
}
