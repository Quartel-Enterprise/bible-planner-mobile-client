package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType

internal class ToggleChapterReadStatusUseCase(
    private val calculateChapterReadStatus: CalculateChapterReadStatusUseCase,
    private val updateChapterReadStatus: UpdateChapterReadStatusUseCase,
) {
    suspend operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        passageIndex: Int,
        chapterIndex: Int,
        passage: PassageModel,
        books: List<BookDataModel>,
        readingPlanType: ReadingPlanType,
    ) {
        // Calculate the new read status
        val newReadStatus = calculateChapterReadStatus(
            passage = passage,
            chapterIndex = chapterIndex,
            books = books,
        ) ?: return

        // Update the chapter read status
        updateChapterReadStatus(
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            passageIndex = passageIndex,
            chapterIndex = chapterIndex,
            isRead = newReadStatus,
            readingPlanType = readingPlanType,
        )
    }
}
