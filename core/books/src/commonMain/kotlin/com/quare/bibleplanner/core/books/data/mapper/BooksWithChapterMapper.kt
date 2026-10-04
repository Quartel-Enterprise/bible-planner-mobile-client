package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.provider.room.relation.BookWithChapters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
 * Why: Room re-emits all 66 books on any read change; memoizing by book.id reuses the
 * unchanged ones and cut ~98% of allocations, the dominant GC pressure on Kotlin/Native.
 */
class BooksWithChapterMapper {
    private val cacheMutex = Mutex()
    private val cache = mutableMapOf<String, CacheEntry>()

    private data class CacheEntry(
        val source: BookWithChapters,
        val result: BookDataModel,
    )

    suspend fun mapList(bookWithChapters: List<BookWithChapters>): List<BookDataModel> =
        bookWithChapters.map { mapModel(it) }

    suspend fun mapModel(bookWithChapters: BookWithChapters): BookDataModel {
        val key = bookWithChapters.book.id
        val cached = cacheMutex.withLock { cache[key] }
        if (cached != null && cached.source == bookWithChapters) {
            return cached.result
        }
        val mapped = computeModel(bookWithChapters)
        cacheMutex.withLock {
            cache[key] = CacheEntry(bookWithChapters, mapped)
        }
        return mapped
    }

    private fun computeModel(bookWithChapters: BookWithChapters): BookDataModel = bookWithChapters.run {
        val chaptersModel = chapters.map { chapterWithVerses ->
            val versesModel = chapterWithVerses.verses.map { verse ->
                VerseModel(
                    number = verse.number,
                    isRead = verse.isRead,
                )
            }

            val isChapterRead = chapterWithVerses.chapter.isRead || (
                versesModel.isNotEmpty() && versesModel.all { it.isRead }
            )

            val readUpdatedAt = (
                listOfNotNull(chapterWithVerses.chapter.readUpdatedAt) +
                    chapterWithVerses.verses.mapNotNull { it.readUpdatedAt }
            ).maxOrNull()

            BookChapterModel(
                number = chapterWithVerses.chapter.number,
                verses = versesModel,
                isRead = isChapterRead,
                readUpdatedAt = readUpdatedAt,
            )
        }

        val isBookRead = book.isRead || (
            chaptersModel.isNotEmpty() && chaptersModel.all { it.isRead }
        )

        BookDataModel(
            id = BookId.valueOf(book.id),
            chapters = chaptersModel,
            isRead = isBookRead,
            isFavorite = book.isFavorite,
        )
    }
}
