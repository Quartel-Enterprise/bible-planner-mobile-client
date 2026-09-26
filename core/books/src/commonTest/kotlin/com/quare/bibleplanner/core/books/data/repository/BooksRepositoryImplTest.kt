package com.quare.bibleplanner.core.books.data.repository

import com.quare.bibleplanner.core.books.data.datasource.BooksLocalDataSource
import com.quare.bibleplanner.core.books.data.mapper.BooksWithChapterMapper
import com.quare.bibleplanner.core.books.data.mapper.FileNameToBookIdMapper
import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.books.fake.InMemoryPreferencesDataStore
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BooksRepositoryImplTest {
    private val now = 1_700_000_000_000L

    private lateinit var database: FakeReadingDatabase
    private lateinit var repository: BooksRepositoryImpl

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(2),
            readVerses = mapOf(1 to setOf(1, 2)),
        )
        database.seedBook(
            bookId = BookId.EXO,
            versesPerChapter = listOf(1, 1),
        )
        repository = BooksRepositoryImpl(
            booksLocalDataSource = BooksLocalDataSource(FileNameToBookIdMapper(BookMapsProvider())),
            bookDao = database.bookDao,
            chapterDao = database.chapterDao,
            verseDao = database.verseDao,
            booksWithChapterMapper = BooksWithChapterMapper(),
            dataStore = InMemoryPreferencesDataStore(),
            currentTimestampProvider = CurrentTimestampProvider { now },
        )
    }

    @Test
    fun `GIVEN stored books WHEN observing them THEN maps each book with its derived read state`() = runTest {
        // When
        val books = repository.getBooksFlow().first()

        // Then
        assertEquals(listOf(BookId.GEN to true, BookId.EXO to false), books.map { it.id to it.isRead })
        assertEquals(listOf(1, 2), books.last().chapters.map { it.number })
    }

    @Test
    fun `GIVEN stored books WHEN reading them once THEN returns the same books as the flow`() = runTest {
        // When
        val books = repository.getBooks()

        // Then
        assertEquals(repository.getBooksFlow().first(), books)
    }

    @Test
    fun `GIVEN a stored book WHEN observing it by id THEN emits that book`() = runTest {
        // When
        val book = repository.getBookByIdFlow(BookId.EXO).first()

        // Then
        assertEquals(BookId.EXO, book?.id)
    }

    @Test
    fun `GIVEN a missing book WHEN observing it by id THEN emits null`() = runTest {
        // When
        val book = repository.getBookByIdFlow(BookId.REV).first()

        // Then
        assertNull(book)
    }

    @Test
    fun `GIVEN a populated database WHEN initializing THEN keeps the existing rows untouched`() = runTest {
        // When
        repository.initializeDatabase()

        // Then
        assertEquals(listOf("GEN", "EXO"), database.books.map { it.id })
    }

    @Test
    fun `GIVEN a favorite change WHEN storing it THEN stamps it with the current time`() = runTest {
        // When
        repository.updateBookFavoriteStatus(
            bookId = BookId.EXO,
            isFavorite = true,
        )

        // Then
        assertEquals(
            listOf(
                Triple(
                    first = "EXO",
                    second = true,
                    third = now,
                ),
            ),
            database.favoriteUpdates,
        )
    }

    @Test
    fun `GIVEN nothing stored WHEN observing the layout format and testament THEN both are null`() = runTest {
        // When
        val layoutFormat = repository.getBookLayoutFormatFlow().first()
        val testament = repository.getSelectedTestamentFlow().first()

        // Then
        assertNull(layoutFormat)
        assertNull(testament)
    }

    @Test
    fun `GIVEN a layout format WHEN storing it THEN the layout format flow emits it`() = runTest {
        // When
        repository.setBookLayoutFormat("grid")

        // Then
        assertEquals("grid", repository.getBookLayoutFormatFlow().first())
    }

    @Test
    fun `GIVEN a testament WHEN storing it THEN the testament flow emits it without touching the layout`() = runTest {
        // When
        repository.setSelectedTestament("new")

        // Then
        assertEquals("new", repository.getSelectedTestamentFlow().first())
        assertNull(repository.getBookLayoutFormatFlow().first())
    }
}
