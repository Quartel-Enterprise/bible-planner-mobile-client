package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.books.domain.isChapterRead
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.plan.PassageModel

class CalculateAllChaptersReadStatusUseCase {
    operator fun invoke(
        passages: List<PassageModel>,
        books: List<BookDataModel>,
    ): Map<Pair<Int, Int>, Boolean> {
        val statusMap = mutableMapOf<Pair<Int, Int>, Boolean>()

        passages.forEachIndexed { passageIndex, passage ->
            val book = books.find { it.id == passage.bookId }
            passage.chapters.forEachIndexed { chapterIndex, chapter ->
                statusMap[passageIndex to chapterIndex] = book.isChapterRead(chapter)
            }
        }

        return statusMap
    }
}
