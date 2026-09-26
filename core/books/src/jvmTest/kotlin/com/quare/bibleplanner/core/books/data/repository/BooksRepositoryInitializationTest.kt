package com.quare.bibleplanner.core.books.data.repository

import com.quare.bibleplanner.core.books.data.datasource.BooksLocalDataSource
import com.quare.bibleplanner.core.books.data.mapper.BooksWithChapterMapper
import com.quare.bibleplanner.core.books.data.mapper.FileNameToBookIdMapper
import com.quare.bibleplanner.core.books.data.provider.BookMapsProvider
import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.books.fake.InMemoryPreferencesDataStore
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BooksRepositoryInitializationTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var repository: BooksRepositoryImpl

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        repository = BooksRepositoryImpl(
            booksLocalDataSource = BooksLocalDataSource(FileNameToBookIdMapper(BookMapsProvider())),
            bookDao = database.bookDao,
            chapterDao = database.chapterDao,
            verseDao = database.verseDao,
            booksWithChapterMapper = BooksWithChapterMapper(),
            dataStore = InMemoryPreferencesDataStore(),
            currentTimestampProvider = CurrentTimestampProvider { 0L },
        )
    }

    @Test
    fun `GIVEN an empty database WHEN initializing THEN inserts every bundled book unread and not favorite`() =
        runTest {
            // When
            repository.initializeDatabase()

            // Then
            assertEquals(BookId.entries.map { it.name }, database.books.map { it.id })
            assertTrue(database.books.none { it.isRead || it.isFavorite || it.isFavoritePendingSync })
        }

    @Test
    fun `GIVEN an empty database WHEN initializing THEN links every verse to its own chapter`() = runTest {
        // When
        repository.initializeDatabase()

        // Then
        val genesisFirstChapter = database.chapter(
            bookId = BookId.GEN,
            chapterNumber = 1,
        )
        val versesOfChapter = database.verses.filter { it.chapterId == genesisFirstChapter.id }
        assertEquals(1189, database.chapters.size)
        assertEquals((1..31).toList(), versesOfChapter.map { it.number })
    }

    @Test
    fun `GIVEN an initialized database WHEN initializing again THEN does not duplicate the rows`() = runTest {
        // Given
        repository.initializeDatabase()

        // When
        repository.initializeDatabase()

        // Then
        assertEquals(66, database.books.size)
        assertEquals(1189, database.chapters.size)
    }
}
