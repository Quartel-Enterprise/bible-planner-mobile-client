package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.FakeReadingDatabase
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class UpdatePassageReadStatusUseCaseTest {
    private lateinit var database: FakeReadingDatabase
    private lateinit var useCase: UpdatePassageReadStatusUseCase

    @BeforeTest
    fun setUp() {
        database = FakeReadingDatabase()
        database.seedBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(3, 3),
        )
        database.seedBook(
            bookId = BookId.OBA,
            versesPerChapter = listOf(2),
        )
        val timestampProvider = CurrentTimestampProvider { 0L }
        val trackEvent = TrackEvent { _, _ -> }
        val updateWholeBookReadStatusIfNeeded = UpdateWholeBookReadStatusIfNeededUseCase(
            bookDao = database.bookDao,
            chapterDao = database.chapterDao,
            trackEvent = trackEvent,
        )
        val isChapterRead = IsChapterReadUseCase(
            chapterDao = database.chapterDao,
            verseDao = database.verseDao,
            isWholeChapterRead = IsWholeChapterReadUseCase(
                chapterDao = database.chapterDao,
                verseDao = database.verseDao,
            ),
        )
        useCase = UpdatePassageReadStatusUseCase(
            updateBookReadStatus = UpdateBookReadStatusUseCase(
                bookDao = database.bookDao,
                chapterDao = database.chapterDao,
                verseDao = database.verseDao,
                currentTimestampProvider = timestampProvider,
                trackEvent = trackEvent,
            ),
            areAllPassagesRead = AreAllPassagesReadUseCase(
                IsPassageReadUseCase(
                    bookDao = database.bookDao,
                    chapterDao = database.chapterDao,
                    isChapterRead = isChapterRead,
                ),
            ),
            updateWholeChapterReadStatus = UpdateWholeChapterReadStatusUseCase(
                chapterDao = database.chapterDao,
                verseDao = database.verseDao,
                updateWholeBookReadStatusIfNeeded = updateWholeBookReadStatusIfNeeded,
                currentTimestampProvider = timestampProvider,
            ),
            updateSpecificRangeChapterReadStatus = UpdateSpecificRangeChapterReadStatusUseCase(
                chapterDao = database.chapterDao,
                verseDao = database.verseDao,
                updateWholeBookReadStatusIfNeeded = updateWholeBookReadStatusIfNeeded,
                currentTimestampProvider = timestampProvider,
            ),
        )
    }

    @Test
    fun `GIVEN no passages WHEN updating THEN writes nothing`() = runTest {
        // When
        useCase(emptyList())

        // Then
        assertTrue(database.chapterReadUpdates.isEmpty())
        assertTrue(database.verseRangeUpdates.isEmpty())
    }

    @Test
    fun `GIVEN an unread whole-book passage WHEN updating THEN marks the whole book read`() = runTest {
        // When
        useCase(passage(bookId = BookId.OBA))

        // Then
        assertTrue(database.book(BookId.OBA).isRead)
        assertEquals(
            listOf(1, 2),
            database.readVerseNumbers(
                bookId = BookId.OBA,
                chapterNumber = 1,
            ),
        )
    }

    @Test
    fun `GIVEN a read whole-book passage WHEN updating THEN marks the whole book unread`() = runTest {
        // Given
        useCase(passage(bookId = BookId.OBA))

        // When
        useCase(passage(bookId = BookId.OBA))

        // Then
        assertFalse(database.book(BookId.OBA).isRead)
        assertEquals(
            emptyList(),
            database.readVerseNumbers(
                bookId = BookId.OBA,
                chapterNumber = 1,
            ),
        )
    }

    @Test
    fun `GIVEN whole chapters WHEN updating THEN marks every listed chapter read`() = runTest {
        // When
        useCase(
            passage(
                bookId = BookId.GEN,
                chapters = listOf(
                    chapter(number = 1),
                    chapter(number = 2),
                ),
            ),
        )

        // Then
        assertTrue(database.book(BookId.GEN).isRead)
        assertEquals(
            listOf(1, 2),
            database.chapterReadUpdates.map { it.first.toInt() },
        )
    }

    @Test
    fun `GIVEN a verse range WHEN updating THEN marks only that range read`() = runTest {
        // When
        useCase(
            passage(
                bookId = BookId.GEN,
                chapters = listOf(
                    chapter(
                        number = 2,
                        startVerse = 1,
                        endVerse = 2,
                    ),
                ),
            ),
        )

        // Then
        assertEquals(
            listOf(1, 2),
            database.readVerseNumbers(
                bookId = BookId.GEN,
                chapterNumber = 2,
            ),
        )
        assertFalse(database.book(BookId.GEN).isRead)
    }

    @Test
    fun `GIVEN one passage already read and another unread WHEN updating both THEN marks both read`() = runTest {
        // Given
        useCase(passage(bookId = BookId.OBA))

        // When
        useCase(
            listOf(
                passage(bookId = BookId.OBA),
                passage(
                    bookId = BookId.GEN,
                    chapters = listOf(chapter(number = 1)),
                ),
            ),
        )

        // Then
        assertTrue(database.book(BookId.OBA).isRead)
        assertEquals(
            listOf(1, 2, 3),
            database.readVerseNumbers(
                bookId = BookId.GEN,
                chapterNumber = 1,
            ),
        )
    }

    private fun passage(
        bookId: BookId,
        chapters: List<ChapterModel> = emptyList(),
    ): PassageModel = PassageModel(
        bookId = bookId,
        chapters = chapters,
        isRead = false,
        chapterRanges = null,
    )

    private fun chapter(
        number: Int,
        startVerse: Int? = null,
        endVerse: Int? = null,
    ): ChapterModel = ChapterModel(
        number = number,
        startVerse = startVerse,
        endVerse = endVerse,
        bookId = BookId.GEN,
    )
}
