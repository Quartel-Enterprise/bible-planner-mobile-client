package com.quare.bibleplanner.core.books.fake

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.room.relation.BookWithChapters
import com.quare.bibleplanner.core.provider.room.relation.ChapterWithVerses
import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeReadingDatabase {
    val books = mutableListOf<BookEntity>()
    val chapters = mutableListOf<ChapterEntity>()
    val verses = mutableListOf<VerseEntity>()
    val verseTexts = mutableListOf<VerseTextEntity>()
    val favoriteUpdates = mutableListOf<Triple<String, Boolean, Long>>()
    val chapterReadUpdates = mutableListOf<Triple<Long, Boolean, Long>>()
    val verseRangeUpdates = mutableListOf<VerseRangeUpdate>()
    val syncResets = mutableListOf<Pair<String, Long>>()

    private val version = MutableStateFlow(0)
    private var lastChapterId = 0L
    private var lastVerseId = 0L

    val bookDao: BookDao = FakeBookDao()
    val chapterDao: ChapterDao = FakeChapterDao()
    val verseDao: VerseDao = FakeVerseDao()

    fun seedBook(
        bookId: BookId,
        versesPerChapter: List<Int>,
        isBookRead: Boolean = false,
        readChapters: Set<Int> = emptySet(),
        readVerses: Map<Int, Set<Int>> = emptyMap(),
    ) {
        books += BookEntity(
            id = bookId.name,
            isRead = isBookRead,
            isFavorite = false,
            favoriteUpdatedAt = null,
            isFavoritePendingSync = false,
        )
        versesPerChapter.forEachIndexed { index, verseCount ->
            val chapterNumber = index + 1
            val chapterId = nextChapterId()
            chapters += ChapterEntity(
                id = chapterId,
                number = chapterNumber,
                bookId = bookId.name,
                isRead = chapterNumber in readChapters,
            )
            (1..verseCount).forEach { verseNumber ->
                verses += VerseEntity(
                    id = nextVerseId(),
                    number = verseNumber,
                    chapterId = chapterId,
                    isRead = verseNumber in readVerses[chapterNumber].orEmpty(),
                )
            }
        }
        touch()
    }

    fun chapter(
        bookId: BookId,
        chapterNumber: Int,
    ): ChapterEntity = chapters.first { it.bookId == bookId.name && it.number == chapterNumber }

    fun book(bookId: BookId): BookEntity = books.first { it.id == bookId.name }

    fun readVerseNumbers(
        bookId: BookId,
        chapterNumber: Int,
    ): List<Int> {
        val chapterId = chapter(
            bookId = bookId,
            chapterNumber = chapterNumber,
        ).id
        return verses.filter { it.chapterId == chapterId && it.isRead }.map { it.number }
    }

    private fun nextChapterId(): Long = ++lastChapterId

    private fun nextVerseId(): Long = ++lastVerseId

    private fun touch() {
        version.value += 1
    }

    private fun booksWithChapters(): List<BookWithChapters> = books.map(::bookWithChapters)

    private fun bookWithChapters(book: BookEntity): BookWithChapters = BookWithChapters(
        book = book,
        chapters = chapters
            .filter { it.bookId == book.id }
            .map { chapter ->
                ChapterWithVerses(
                    chapter = chapter,
                    verses = verses.filter { it.chapterId == chapter.id },
                )
            },
    )

    private fun versesWithTexts(chapterId: Long): List<VerseWithTexts> = verses
        .filter { it.chapterId == chapterId }
        .map { verse ->
            VerseWithTexts(
                verse = verse,
                texts = verseTexts.filter { it.verseId == verse.id },
            )
        }

    private fun updateChapters(
        predicate: (ChapterEntity) -> Boolean,
        transform: (ChapterEntity) -> ChapterEntity,
    ) {
        chapters.replaceAll { if (predicate(it)) transform(it) else it }
        touch()
    }

    private fun updateVerses(
        predicate: (VerseEntity) -> Boolean,
        transform: (VerseEntity) -> VerseEntity,
    ) {
        verses.replaceAll { if (predicate(it)) transform(it) else it }
        touch()
    }

    private fun chapterIdsOf(bookId: String): Set<Long> = chapters.filter { it.bookId == bookId }.map { it.id }.toSet()

    private inner class FakeBookDao : ThrowingBookDao() {
        override suspend fun getBookById(bookId: String): BookEntity? = books.find { it.id == bookId }

        override suspend fun getAllBooksWithChapters(): List<BookWithChapters> = booksWithChapters()

        override fun getAllBooksWithChaptersFlow(): Flow<List<BookWithChapters>> = version.map { booksWithChapters() }

        override fun getBookWithChaptersByIdFlow(bookId: String): Flow<BookWithChapters?> = version.map {
            books.find { it.id == bookId }?.let(::bookWithChapters)
        }

        override suspend fun insertBooks(books: List<BookEntity>) {
            this@FakeReadingDatabase.books += books
            touch()
        }

        override suspend fun updateBookReadStatus(
            bookId: String,
            isRead: Boolean,
        ) {
            books.replaceAll { if (it.id == bookId) it.copy(isRead = isRead) else it }
            touch()
        }

        override suspend fun updateBookFavoriteStatus(
            bookId: String,
            isFavorite: Boolean,
            updatedAt: Long,
        ) {
            favoriteUpdates += Triple(
                first = bookId,
                second = isFavorite,
                third = updatedAt,
            )
        }

        override suspend fun resetAllBooksProgress() {
            books.replaceAll { it.copy(isRead = false) }
            touch()
        }
    }

    private inner class FakeChapterDao : ThrowingChapterDao() {
        override suspend fun getChaptersByBookId(bookId: String): List<ChapterEntity> =
            chapters.filter { it.bookId == bookId }

        override suspend fun getChapterByBookIdAndNumber(
            bookId: String,
            chapterNumber: Int,
        ): ChapterEntity? = chapters.find { it.bookId == bookId && it.number == chapterNumber }

        override suspend fun insertChapters(chapters: List<ChapterEntity>): List<Long> = chapters.map { chapter ->
            val id = nextChapterId()
            this@FakeReadingDatabase.chapters += chapter.copy(id = id)
            id
        }

        override suspend fun updateChapterReadStatus(
            chapterId: Long,
            isRead: Boolean,
            updatedAt: Long,
        ) {
            chapterReadUpdates += Triple(
                first = chapterId,
                second = isRead,
                third = updatedAt,
            )
            updateChapters(
                predicate = { it.id == chapterId },
                transform = { it.copy(isRead = isRead, readUpdatedAt = updatedAt) },
            )
        }

        override suspend fun updateChaptersReadStatusByBook(
            bookId: String,
            isRead: Boolean,
            updatedAt: Long,
        ) {
            updateChapters(
                predicate = { it.bookId == bookId },
                transform = { it.copy(isRead = isRead, readUpdatedAt = updatedAt) },
            )
        }

        override suspend fun resetAllChapterReadsForSync(now: Long) {
            syncResets += "chapters" to now
            updateChapters(
                predicate = { true },
                transform = { it.copy(isRead = false) },
            )
        }
    }

    private inner class FakeVerseDao : ThrowingVerseDao() {
        override suspend fun getVersesByChapterId(chapterId: Long): List<VerseEntity> =
            verses.filter { it.chapterId == chapterId }

        override suspend fun getVersesWithTextsByChapterId(chapterId: Long): List<VerseWithTexts> =
            versesWithTexts(chapterId)

        override fun getVersesWithTextsByChapterIdFlow(chapterId: Long): Flow<List<VerseWithTexts>> =
            version.map { versesWithTexts(chapterId) }

        override suspend fun upsertVerses(verses: List<VerseEntity>): List<Long> = verses.map { verse ->
            val id = nextVerseId()
            this@FakeReadingDatabase.verses += verse.copy(id = id)
            id
        }

        override suspend fun updateVersesReadStatusByChapter(
            chapterId: Long,
            isRead: Boolean,
        ) {
            updateVerses(
                predicate = { it.chapterId == chapterId },
                transform = { it.copy(isRead = isRead) },
            )
        }

        override suspend fun updateVersesReadStatusByBook(
            bookId: String,
            isRead: Boolean,
        ) {
            val chapterIds = chapterIdsOf(bookId)
            updateVerses(
                predicate = { it.chapterId in chapterIds },
                transform = { it.copy(isRead = isRead) },
            )
        }

        override suspend fun updateVerseReadStatusRange(
            chapterId: Long,
            startVerse: Int,
            endVerse: Int,
            isRead: Boolean,
            updatedAt: Long,
        ) {
            verseRangeUpdates += VerseRangeUpdate(
                chapterId = chapterId,
                startVerse = startVerse,
                endVerse = endVerse,
                isRead = isRead,
                updatedAt = updatedAt,
            )
            updateVerses(
                predicate = { it.chapterId == chapterId && it.number in startVerse..endVerse },
                transform = { it.copy(isRead = isRead, readUpdatedAt = updatedAt) },
            )
        }

        override suspend fun resetAllVerseReadsForSync(now: Long) {
            syncResets += "verses" to now
            updateVerses(
                predicate = { true },
                transform = { it.copy(isRead = false) },
            )
        }
    }
}
