package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.fake.ThrowingBookDao
import com.quare.bibleplanner.core.books.fake.ThrowingChapterDao
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class UpdateWholeBookReadStatusIfNeededUseCaseTest {
    private lateinit var useCase: UpdateWholeBookReadStatusIfNeededUseCase
    private lateinit var bookDao: RecordingBookDao
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @Test
    fun `GIVEN an unread book WHEN its last chapter becomes read THEN fires book_completed with last_chapter source`() =
        runTest {
            // Given
            prepareScenario(
                isBookRead = false,
                chaptersRead = listOf(true, true),
            )

            // When
            useCase(BookId.GEN)

            // Then
            assertEquals(true, bookDao.lastReadStatusUpdate)
            assertEquals(
                listOf(
                    "book_completed" to mapOf<String, Any>(
                        "book_id" to "gen",
                        "source" to "last_chapter",
                    ),
                ),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN an already read book WHEN all chapters remain read THEN fires nothing`() = runTest {
        // Given
        prepareScenario(
            isBookRead = true,
            chaptersRead = listOf(true, true),
        )

        // When
        useCase(BookId.GEN)

        // Then
        assertEquals(null, bookDao.lastReadStatusUpdate)
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a read book WHEN a chapter becomes unread THEN unmarks the book without firing`() = runTest {
        // Given
        prepareScenario(
            isBookRead = true,
            chaptersRead = listOf(true, false),
        )

        // When
        useCase(BookId.GEN)

        // Then
        assertEquals(false, bookDao.lastReadStatusUpdate)
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a book without chapters WHEN invoked THEN fires nothing`() = runTest {
        // Given
        prepareScenario(
            isBookRead = false,
            chaptersRead = emptyList(),
        )

        // When
        useCase(BookId.GEN)

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    private fun prepareScenario(
        isBookRead: Boolean,
        chaptersRead: List<Boolean>,
    ) {
        trackedEvents = mutableListOf()
        bookDao = RecordingBookDao(
            book = BookEntity(
                id = BookId.GEN.name,
                isRead = isBookRead,
                isFavorite = false,
                favoriteUpdatedAt = null,
                isFavoritePendingSync = false,
            ),
        )
        useCase = UpdateWholeBookReadStatusIfNeededUseCase(
            bookDao = bookDao,
            chapterDao = FixedChaptersDao(
                chapters = chaptersRead.mapIndexed { index, isRead ->
                    ChapterEntity(
                        id = index + 1L,
                        number = index + 1,
                        bookId = BookId.GEN.name,
                        isRead = isRead,
                    )
                },
            ),
            trackEvent = TrackEvent { name, params -> trackedEvents += name to params },
        )
    }
}

private class RecordingBookDao(
    private val book: BookEntity?,
) : ThrowingBookDao() {
    var lastReadStatusUpdate: Boolean? = null

    override suspend fun getBookById(bookId: String): BookEntity? = book

    override suspend fun updateBookReadStatus(
        bookId: String,
        isRead: Boolean,
    ) {
        lastReadStatusUpdate = isRead
    }
}

private class FixedChaptersDao(
    private val chapters: List<ChapterEntity>,
) : ThrowingChapterDao() {
    override suspend fun getChaptersByBookId(bookId: String): List<ChapterEntity> = chapters
}
