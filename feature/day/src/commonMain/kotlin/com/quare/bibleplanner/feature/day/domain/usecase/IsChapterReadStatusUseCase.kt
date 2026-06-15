package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.books.domain.isRangeRead
import com.quare.bibleplanner.core.books.domain.usecase.GetBooksFlowUseCase
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import kotlinx.coroutines.flow.first

internal class IsChapterReadStatusUseCase(
    private val getBooksFlow: GetBooksFlowUseCase,
) {
    /**
     * Calculates the new read status for a chapter after toggling.
     * Returns the new status (toggled from the current status).
     *
     * @param passage The passage containing the chapter
     * @param strategy The strategy to determine the new read status
     * @return The new read status (true if should be marked as read, false otherwise)
     * @return null if the chapter index is invalid
     */
    suspend operator fun invoke(
        passage: PassageModel,
        strategy: UpdateReadStatusOfPassageStrategy,
    ): Result<Boolean> = when (strategy) {
        is UpdateReadStatusOfPassageStrategy.Chapter -> calculateFromSafeChapterIndex(
            chapterIndex = strategy.chapterIndex,
            passage = passage,
        )

        is UpdateReadStatusOfPassageStrategy.EntireBook -> Result.success(!passage.isRead)
    }

    private suspend fun calculateFromSafeChapterIndex(
        chapterIndex: Int,
        passage: PassageModel,
    ): Result<Boolean> {
        val errorResult = Result.failure<Boolean>(IllegalStateException())
        if (chapterIndex < 0 || chapterIndex >= passage.chapters.size) {
            return errorResult
        }
        val chapter = passage.chapters[chapterIndex]
        val book = getBooksFlow().first().find { it.id == passage.bookId } ?: return errorResult
        val bookChapter = book.chapters.find { it.number == chapter.number } ?: return errorResult

        // Check if a chapter is read based on verse ranges
        val isCurrentlyRead = bookChapter.isRangeRead(chapter.startVerse, chapter.endVerse)
        return Result.success(!isCurrentlyRead)
    }
}
