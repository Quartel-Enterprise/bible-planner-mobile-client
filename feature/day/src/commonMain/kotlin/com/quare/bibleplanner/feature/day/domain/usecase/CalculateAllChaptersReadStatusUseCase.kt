package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterPlanModel
import com.quare.bibleplanner.core.model.plan.PassagePlanModel

class CalculateAllChaptersReadStatusUseCase {
    /**
     * Calculate the read status for each chapter in each passage.
     * Returns a map where the key is (passageIndex, chapterIndex) and the value is whether the chapter is read.
     */
    operator fun invoke(
        passages: List<PassagePlanModel>,
        books: List<BookDataModel>,
    ): Map<Pair<Int, Int>, Boolean> {
        val statusMap = mutableMapOf<Pair<Int, Int>, Boolean>()

        passages.forEachIndexed { passageIndex, passage ->
            passage.chapters.forEachIndexed { chapterIndex, chapter ->
                val isRead = isChapterRead(passage, chapter, books)
                statusMap[passageIndex to chapterIndex] = isRead
            }
        }

        return statusMap
    }

    /**
     * Check if a specific chapter within a passage is read by checking the book data.
     */
    private fun isChapterRead(
        passage: PassagePlanModel,
        chapter: ChapterPlanModel,
        books: List<BookDataModel>,
    ): Boolean {
        val book = books.find { it.id == passage.bookId } ?: return false
        val bookChapter = book.chapters.find { it.number == chapter.number } ?: return false

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
