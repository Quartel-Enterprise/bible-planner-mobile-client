package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.ThrowingBookDao
import com.quare.bibleplanner.core.books.fake.ThrowingChapterDao
import com.quare.bibleplanner.core.books.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class UpdateBookReadStatusUseCaseTest {
    private lateinit var useCase: UpdateBookReadStatusUseCase
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @Test
    fun `GIVEN a book with unread chapters WHEN marking it read THEN fires book_completed with toggle_all source`() =
        runTest {
            // Given
            prepareScenario(chaptersRead = listOf(true, false))

            // When
            useCase(
                bookId = BookId.GEN,
                isRead = true,
            )

            // Then
            assertEquals(
                listOf(
                    "book_completed" to mapOf<String, Any>(
                        "book_id" to "gen",
                        "source" to "toggle_all",
                    ),
                ),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN a fully read book WHEN marking it read again THEN fires nothing`() = runTest {
        // Given
        prepareScenario(chaptersRead = listOf(true, true))

        // When
        useCase(
            bookId = BookId.GEN,
            isRead = true,
        )

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a fully read book WHEN marking it unread THEN fires nothing`() = runTest {
        // Given
        prepareScenario(chaptersRead = listOf(true, true))

        // When
        useCase(
            bookId = BookId.GEN,
            isRead = false,
        )

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    private fun prepareScenario(chaptersRead: List<Boolean>) {
        trackedEvents = mutableListOf()
        useCase = UpdateBookReadStatusUseCase(
            bookDao = SilentBookDao(),
            chapterDao = SilentChapterDao(
                chapters = chaptersRead.mapIndexed { index, isRead ->
                    ChapterEntity(
                        id = index + 1L,
                        number = index + 1,
                        bookId = BookId.GEN.name,
                        isRead = isRead,
                    )
                },
            ),
            verseDao = SilentVerseDao(),
            currentTimestampProvider = CurrentTimestampProvider { 0L },
            trackEvent = TrackEvent { name, params -> trackedEvents += name to params },
        )
    }
}

private class SilentBookDao : ThrowingBookDao() {
    override suspend fun updateBookReadStatus(
        bookId: String,
        isRead: Boolean,
    ) = Unit
}

private class SilentChapterDao(
    private val chapters: List<ChapterEntity>,
) : ThrowingChapterDao() {
    override suspend fun getChaptersByBookId(bookId: String): List<ChapterEntity> = chapters

    override suspend fun updateChaptersReadStatusByBook(
        bookId: String,
        isRead: Boolean,
        updatedAt: Long,
    ) = Unit
}

private class SilentVerseDao : ThrowingVerseDao() {
    override suspend fun updateVersesReadStatusByBook(
        bookId: String,
        isRead: Boolean,
    ) = Unit
}
