package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.books.domain.isRangeRead
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel

class CalculateAllChaptersReadStatusUseCase {
    operator fun invoke(
        passages: List<PassageModel>,
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

    private fun isChapterRead(
        passage: PassageModel,
        chapter: ChapterModel,
        books: List<BookDataModel>,
    ): Boolean {
        val book = books.find { it.id == passage.bookId } ?: return false
        val bookChapter = book.chapters.find { it.number == chapter.number } ?: return false
        return bookChapter.isRangeRead(chapter.startVerse, chapter.endVerse)
    }
}
