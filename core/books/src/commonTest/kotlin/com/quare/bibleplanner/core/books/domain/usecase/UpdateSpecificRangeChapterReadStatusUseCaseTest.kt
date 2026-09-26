package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.books.fake.VerseRangeUpdate
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class UpdateSpecificRangeChapterReadStatusUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: UpdateSpecificRangeChapterReadStatusUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.JUD,
            versesPerChapter = listOf(4),
            readVerses = mapOf(1 to setOf(1, 2)),
        )
        useCase = UpdateSpecificRangeChapterReadStatusUseCase(
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
    fun `GIVEN a partially read chapter WHEN marking a range read THEN marks only the verses in that range`() =
        runTest {
            // When
            useCase(
                chapterNumber = 1,
                startVerse = 2,
                endVerse = 3,
                isRead = true,
                bookId = BookId.JUD,
            )

            // Then
            val chapterId = database
                .chapter(
                    bookId = BookId.JUD,
                    chapterNumber = 1,
                ).id
            assertEquals(
                listOf(
                    VerseRangeUpdate(
                        chapterId = chapterId,
                        startVerse = 2,
                        endVerse = 3,
                        isRead = true,
                        updatedAt = TIMESTAMP,
                    ),
                ),
                database.verseRangeUpdates,
            )
            assertEquals(
                listOf(1, 2, 3),
                database.readVerseNumbers(
                    bookId = BookId.JUD,
                    chapterNumber = 1,
                ),
            )
            assertTrue(database.chapterReadUpdates.isEmpty())
        }

    @Test
    fun `GIVEN the range completes the chapter WHEN marking it read THEN the chapter and its book become read`() =
        runTest {
            // When
            useCase(
                chapterNumber = 1,
                startVerse = 3,
                endVerse = 4,
                isRead = true,
                bookId = BookId.JUD,
            )

            // Then
            assertTrue(
                database
                    .chapter(
                        bookId = BookId.JUD,
                        chapterNumber = 1,
                    ).isRead,
            )
            assertTrue(database.book(BookId.JUD).isRead)
        }

    @Test
    fun `GIVEN a read chapter WHEN unmarking a range THEN the chapter is no longer read`() = runTest {
        // Given
        useCase(
            chapterNumber = 1,
            startVerse = 1,
            endVerse = 4,
            isRead = true,
            bookId = BookId.JUD,
        )

        // When
        useCase(
            chapterNumber = 1,
            startVerse = 4,
            endVerse = 4,
            isRead = false,
            bookId = BookId.JUD,
        )

        // Then
        assertFalse(
            database
                .chapter(
                    bookId = BookId.JUD,
                    chapterNumber = 1,
                ).isRead,
        )
        assertFalse(database.book(BookId.JUD).isRead)
    }

    @Test
    fun `GIVEN a chapter missing from the database WHEN updating a range THEN writes nothing`() = runTest {
        // When
        useCase(
            chapterNumber = 2,
            startVerse = 1,
            endVerse = 2,
            isRead = true,
            bookId = BookId.JUD,
        )

        // Then
        assertTrue(database.verseRangeUpdates.isEmpty())
    }

    private companion object {
        const val TIMESTAMP = 1_700_000_000_000L
    }
}
