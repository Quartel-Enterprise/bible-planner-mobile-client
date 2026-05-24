package com.quare.bibleplanner.core.books.data.mapper

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.provider.room.relation.BookWithChapters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Maps Room relation entities ([BookWithChapters]) to domain models ([BookDataModel]).
 *
 * Memoized by `book.id`: a click that marks 1 verse as read causes Room to re-emit the
 * full list of 66 books, but only 1 book's entity tree actually changed. The cache lets
 * us return the previous [BookDataModel] reference for the other 65 books — avoiding
 * ~98% of per-emission allocations (VerseModel × ~50k, BookChapterModel × ~2k).
 *
 * This was the dominant GC pressure on iOS/Kotlin Native: each emission allocated the
 * full object tree and immediately discarded it on the next emission, keeping the
 * GC thread busy ~25% of the time.
 *
 * Thread-safety: [mapModel] is suspending and guarded by a [Mutex]. Cache mutations
 * are atomic. Cache size is bounded by the number of books (currently 66).
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
        // Fast path: cache hit with structural equality of the source entity
        val cached = cacheMutex.withLock { cache[key] }
        if (cached != null && cached.source == bookWithChapters) {
            return cached.result
        }
        // Cache miss or entity changed: compute outside the lock to avoid blocking other books
        val mapped = computeModel(bookWithChapters)
        cacheMutex.withLock {
            cache[key] = CacheEntry(bookWithChapters, mapped)
        }
        return mapped
    }

    private fun computeModel(bookWithChapters: BookWithChapters): BookDataModel = bookWithChapters.run {
        val chaptersModel = chapters.map { chapterWithVerses ->
            val versesModel = chapterWithVerses.verses.flatMap { verseWithTexts ->
                if (verseWithTexts.texts.isNotEmpty()) {
                    verseWithTexts.texts.map { textEntity ->
                        VerseModel(
                            number = verseWithTexts.verse.number,
                            isRead = verseWithTexts.verse.isRead,
                            text = textEntity.text,
                        )
                    }
                } else {
                    listOf(
                        VerseModel(
                            number = verseWithTexts.verse.number,
                            isRead = verseWithTexts.verse.isRead,
                            text = null,
                        ),
                    )
                }
            }

            // Derive chapter read status: either the flag is true, or all verses are read
            val isChapterRead = chapterWithVerses.chapter.isRead || (
                versesModel.isNotEmpty() && versesModel.all { it.isRead }
            )

            BookChapterModel(
                number = chapterWithVerses.chapter.number,
                verses = versesModel,
                isRead = isChapterRead,
            )
        }

        // Derive book read status: either the flag is true, or all chapters are read
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
