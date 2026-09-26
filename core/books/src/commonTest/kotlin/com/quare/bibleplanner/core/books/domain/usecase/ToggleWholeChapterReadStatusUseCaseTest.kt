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

internal class ToggleWholeChapterReadStatusUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: ToggleWholeChapterReadStatusUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.RUT,
            versesPerChapter = listOf(2, 2),
            readVerses = mapOf(2 to setOf(1, 2)),
        )
        useCase = ToggleWholeChapterReadStatusUseCase(
            isWholeChapterRead = IsWholeChapterReadUseCase(
                chapterDao = database.chapterDao,
                verseDao = database.verseDao,
            ),
            updateWholeChapterRead = UpdateWholeChapterReadStatusUseCase(
                chapterDao = database.chapterDao,
                verseDao = database.verseDao,
                updateWholeBookReadStatusIfNeeded = UpdateWholeBookReadStatusIfNeededUseCase(
                    bookDao = database.bookDao,
                    chapterDao = database.chapterDao,
                    trackEvent = TrackEvent { _, _ -> },
                ),
                currentTimestampProvider = CurrentTimestampProvider { 0L },
            ),
        )
    }

    @Test
    fun `GIVEN an unread chapter WHEN toggling it THEN marks it read and returns true`() = runTest {
        // When
        val result = useCase(
            bookId = BookId.RUT,
            chapterNumber = 1,
        )

        // Then
        assertTrue(result)
        assertEquals(
            listOf(1, 2),
            database.readVerseNumbers(
                bookId = BookId.RUT,
                chapterNumber = 1,
            ),
        )
    }

    @Test
    fun `GIVEN a fully read chapter WHEN toggling it THEN marks it unread and returns false`() = runTest {
        // When
        val result = useCase(
            bookId = BookId.RUT,
            chapterNumber = 2,
        )

        // Then
        assertFalse(result)
        assertEquals(
            emptyList(),
            database.readVerseNumbers(
                bookId = BookId.RUT,
                chapterNumber = 2,
            ),
        )
    }
}
