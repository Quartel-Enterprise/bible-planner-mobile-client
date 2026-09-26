package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class UpdateWholeChapterReadStatusUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: UpdateWholeChapterReadStatusUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.JUD,
            versesPerChapter = listOf(3),
        )
        database.seedBook(
            bookId = BookId.RUT,
            versesPerChapter = listOf(2, 2),
            isBookRead = true,
            readChapters = setOf(1, 2),
            readVerses = mapOf(1 to setOf(1, 2), 2 to setOf(1, 2)),
        )
        useCase = UpdateWholeChapterReadStatusUseCase(
            chapterDao = database.chapterDao,
            verseDao = database.verseDao,
            updateWholeBookReadStatusIfNeeded = UpdateWholeBookReadStatusIfNeededUseCase(
                bookDao = database.bookDao,
                chapterDao = database.chapterDao,
                trackEvent = TrackEvent { _, _ -> },
            ),
            currentTimestampProvider = CurrentTimestampProvider { TIMESTAMP },
        )
    }

    @Test
    fun `GIVEN an unread chapter WHEN marking it read THEN marks it and its verses read at the current time`() =
        runTest {
            // When
            useCase(
                chapterNumber = 1,
                isRead = true,
                bookId = BookId.JUD,
            )

            // Then
            val chapter = database.chapter(
                bookId = BookId.JUD,
                chapterNumber = 1,
            )
            assertTrue(chapter.isRead)
            assertEquals(TIMESTAMP, chapter.readUpdatedAt)
            assertEquals(
                listOf(1, 2, 3),
                database.readVerseNumbers(
                    bookId = BookId.JUD,
                    chapterNumber = 1,
                ),
            )
        }

    @Test
    fun `GIVEN the only chapter of a book WHEN marking it read THEN the book becomes read`() = runTest {
        // When
        useCase(
            chapterNumber = 1,
            isRead = true,
            bookId = BookId.JUD,
        )

        // Then
        assertTrue(database.book(BookId.JUD).isRead)
    }

    @Test
    fun `GIVEN a read book WHEN unmarking one chapter THEN the book is no longer read`() = runTest {
        // When
        useCase(
            chapterNumber = 2,
            isRead = false,
            bookId = BookId.RUT,
        )

        // Then
        assertFalse(database.book(BookId.RUT).isRead)
        assertEquals(
            emptyList(),
            database.readVerseNumbers(
                bookId = BookId.RUT,
                chapterNumber = 2,
            ),
        )
    }

    @Test
    fun `GIVEN a chapter missing from the database WHEN updating it THEN writes nothing`() = runTest {
        // When
        useCase(
            chapterNumber = 7,
            isRead = true,
            bookId = BookId.JUD,
        )

        // Then
        assertTrue(database.chapterReadUpdates.isEmpty())
    }

    private companion object {
        const val TIMESTAMP = 1_700_000_000_000L
    }
}
