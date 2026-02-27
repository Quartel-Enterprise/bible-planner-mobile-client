package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.GetBooksFlowUseCase
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
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
        val isCurrentlyRead = isChapterRead(
            chapter = chapter,
            bookChapter = bookChapter,
        )
        return Result.success(!isCurrentlyRead)
    }

    private fun isChapterRead(
        chapter: ChapterModel,
        bookChapter: BookChapterModel,
    ): Boolean {
        val startVerse = chapter.startVerse
        val endVerse = chapter.endVerse

        return when {
            // If verse range is specified, check those specific verses
            startVerse != null && endVerse != null -> {
                val requiredVerses = startVerse..endVerse
                requiredVerses.all { verseNumber ->
                    bookChapter.verses.find { it.number == verseNumber }?.isRead == true
                }
            }

            // If only start verse is specified, check from that verse to end of chapter
            startVerse != null -> {
                bookChapter.verses
                    .filter { it.number >= startVerse }
                    .all { it.isRead }
            }

            // If no verse range specified, check if entire chapter is read
            else -> {
                bookChapter.isRead
            }
        }
    }
}
