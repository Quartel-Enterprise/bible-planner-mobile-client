package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel

internal class CalculateChapterReadStatusUseCase {
    /**
     * Calculates the new read status for a chapter after toggling.
     * Returns the new status (toggled from current status).
     *
     * @param passage The passage containing the chapter
     * @param chapterIndex The index of the chapter to toggle (-1 for entire book)
     * @param books The list of books to check the actual read status
     * @return The new read status (true if should be marked as read, false otherwise)
     * @return null if the chapter index is invalid
     */
    operator fun invoke(
        passage: PassageModel,
        chapterIndex: Int,
        books: List<BookDataModel>,
    ): Boolean? {
        return if (chapterIndex == -1) {
            // Entire book (no chapters) - toggle the passage read status
            !passage.isRead
        } else {
            // Specific chapter - check if it's currently read
            if (chapterIndex < 0 || chapterIndex >= passage.chapters.size) {
                return null
            }
            val chapter = passage.chapters[chapterIndex]
            val book = books.find { it.id == passage.bookId } ?: return null
            val bookChapter = book.chapters.find { it.number == chapter.number } ?: return null

            // Check if chapter is read based on verse ranges
            val isCurrentlyRead = isChapterRead(chapter, bookChapter)
            !isCurrentlyRead
        }
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
