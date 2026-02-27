package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy

internal class ToggleChapterReadStatusUseCase(
    private val calculateChapterReadStatus: IsChapterReadStatusUseCase,
    private val updateChapterReadStatus: UpdateChapterReadStatusUseCase,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        strategy: UpdateReadStatusOfPassageStrategy,
        passage: PassageModel,
        readingPlanType: ReadingPlanType,
    ): Result<Unit> = calculateChapterReadStatus(
        passage = passage,
        strategy = strategy,
    ).map { newReadStatus ->
        // Update the chapter read status
        updateChapterReadStatus(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            strategy = strategy,
            isRead = newReadStatus,
            readingPlanType = readingPlanType,
        )
    }
}
