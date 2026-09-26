package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.AreAllPassagesReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.IsChapterReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.IsPassageReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterReadUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateBookReadStatusUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdatePassageReadStatusUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateSpecificRangeChapterReadStatusUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateWholeBookReadStatusIfNeededUseCase
import com.quare.bibleplanner.core.books.domain.usecase.UpdateWholeChapterReadStatusUseCase
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.fake.FakeBooksRepository
import com.quare.bibleplanner.core.plan.fake.FakeDayRepository
import com.quare.bibleplanner.core.plan.fake.FakePlanRepository
import com.quare.bibleplanner.core.plan.fake.ThrowingBookDao
import com.quare.bibleplanner.core.plan.fake.ThrowingChapterDao
import com.quare.bibleplanner.core.plan.fake.ThrowingVerseDao
import com.quare.bibleplanner.core.plan.fake.book
import com.quare.bibleplanner.core.plan.fake.bookChapter
import com.quare.bibleplanner.core.plan.fake.day
import com.quare.bibleplanner.core.plan.fake.passage
import com.quare.bibleplanner.core.plan.fake.week
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class UpdateDayReadStatusUseCaseTest {
    private val now = 1_700_000_000_000L

    private lateinit var booksRepository: FakeBooksRepository
    private lateinit var dayRepository: FakeDayRepository
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var useCase: UpdateDayReadStatusUseCase

    @Test
    fun `GIVEN an unread day WHEN marking it read THEN marks its passages read and stamps the day`() = runTest {
        // Given
        prepareScenario(isBookRead = false)

        // When
        useCase(
            weekNumber = 1,
            dayNumber = 1,
            isRead = true,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
        )

        // Then
        assertTrue(
            booksRepository.books.value
                .single()
                .isRead,
        )
        assertEquals(listOf("updateDayReadStatus(1, 1, CHRONOLOGICAL, true, $now)"), dayRepository.calls)
    }

    @Test
    fun `GIVEN the last unread day of the plan WHEN marking it read THEN tracks the completed week and plan`() =
        runTest {
            // Given
            prepareScenario(isBookRead = false)

            // When
            useCase(
                weekNumber = 1,
                dayNumber = 1,
                isRead = true,
                readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            )

            // Then
            assertEquals(
                listOf(
                    "book_completed" to mapOf<String, Any>(
                        "book_id" to "oba",
                        "source" to "toggle_all",
                    ),
                    "week_completed" to mapOf<String, Any>(
                        "plan_type" to "chronological",
                        "week_number" to 1,
                    ),
                    "plan_completed" to mapOf<String, Any>("plan_type" to "chronological"),
                ),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN a read day WHEN marking it unread THEN clears the stamp without tracking completions`() = runTest {
        // Given
        prepareScenario(isBookRead = true)

        // When
        useCase(
            weekNumber = 1,
            dayNumber = 1,
            isRead = false,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
        )

        // Then
        assertEquals(
            false,
            booksRepository.books.value
                .single()
                .isRead,
        )
        assertEquals(listOf("updateDayReadStatus(1, 1, CHRONOLOGICAL, false, null)"), dayRepository.calls)
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a day missing from the plan WHEN updating it THEN changes nothing`() = runTest {
        // Given
        prepareScenario(isBookRead = false)

        // When
        useCase(
            weekNumber = 1,
            dayNumber = 5,
            isRead = true,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
        )
        useCase(
            weekNumber = 1,
            dayNumber = 1,
            isRead = true,
            readingPlanType = ReadingPlanType.BOOKS,
        )

        // Then
        assertTrue(dayRepository.calls.isEmpty())
        assertEquals(
            false,
            booksRepository.books.value
                .single()
                .isRead,
        )
    }

    private fun prepareScenario(isBookRead: Boolean) {
        booksRepository = FakeBooksRepository(
            listOf(
                book(
                    bookId = BookId.OBA,
                    chapters = listOf(
                        bookChapter(
                            number = 1,
                            verseCount = 1,
                            isRead = isBookRead,
                        ),
                    ),
                    isRead = isBookRead,
                ),
            ),
        )
        dayRepository = FakeDayRepository()
        trackedEvents = mutableListOf()
        val trackEvent = TrackEvent { name, params -> trackedEvents += name to params }
        val timestampProvider = CurrentTimestampProvider { now }
        val bookDao = BookStateBookDao(booksRepository)
        val chapterDao = BookStateChapterDao(booksRepository)
        val verseDao = BookStateVerseDao()
        val updateWholeBookReadStatusIfNeeded = UpdateWholeBookReadStatusIfNeededUseCase(
            bookDao = bookDao,
            chapterDao = chapterDao,
            trackEvent = trackEvent,
        )
        useCase = UpdateDayReadStatusUseCase(
            dayRepository = dayRepository,
            updatePassageReadStatus = UpdatePassageReadStatusUseCase(
                updateBookReadStatus = UpdateBookReadStatusUseCase(
                    bookDao = bookDao,
                    chapterDao = chapterDao,
                    verseDao = verseDao,
                    currentTimestampProvider = timestampProvider,
                    trackEvent = trackEvent,
                ),
                areAllPassagesRead = AreAllPassagesReadUseCase(
                    IsPassageReadUseCase(
                        bookDao = bookDao,
                        chapterDao = chapterDao,
                        isChapterRead = IsChapterReadUseCase(
                            chapterDao = chapterDao,
                            verseDao = verseDao,
                            isWholeChapterRead = IsWholeChapterReadUseCase(
                                chapterDao = chapterDao,
                                verseDao = verseDao,
                            ),
                        ),
                    ),
                ),
                updateWholeChapterReadStatus = UpdateWholeChapterReadStatusUseCase(
                    chapterDao = chapterDao,
                    verseDao = verseDao,
                    updateWholeBookReadStatusIfNeeded = updateWholeBookReadStatusIfNeeded,
                    currentTimestampProvider = timestampProvider,
                ),
                updateSpecificRangeChapterReadStatus = UpdateSpecificRangeChapterReadStatusUseCase(
                    chapterDao = chapterDao,
                    verseDao = verseDao,
                    updateWholeBookReadStatusIfNeeded = updateWholeBookReadStatusIfNeeded,
                    currentTimestampProvider = timestampProvider,
                ),
            ),
            getPlansByWeekUseCase = GetPlansByWeekUseCase(
                planRepository = FakePlanRepository(
                    plans = mapOf(
                        ReadingPlanType.CHRONOLOGICAL to listOf(
                            week(
                                number = 1,
                                days = listOf(
                                    day(
                                        number = 1,
                                        passages = listOf(passage(bookId = BookId.OBA)),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    startDate = null,
                    selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
                ),
                booksRepository = booksRepository,
                getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                currentTimestampProvider = timestampProvider,
                localDateTimeProvider = LocalDateTimeProvider {
                    LocalDateTime(
                        year = 2026,
                        month = 1,
                        day = 1,
                        hour = 0,
                        minute = 0,
                    )
                },
            ),
            currentTimestampProvider = timestampProvider,
            trackReadingCompletionEvents = TrackReadingCompletionEventsUseCase(trackEvent),
        )
    }
}

private class BookStateBookDao(
    private val booksRepository: FakeBooksRepository,
) : ThrowingBookDao() {
    override suspend fun getBookById(bookId: String): BookEntity? = booksRepository.books.value
        .find { it.id.name == bookId }
        ?.let { book ->
            BookEntity(
                id = bookId,
                isRead = book.isRead,
                favoriteUpdatedAt = null,
                isFavoritePendingSync = false,
            )
        }

    override suspend fun updateBookReadStatus(
        bookId: String,
        isRead: Boolean,
    ) {
        booksRepository.books.value = booksRepository.books.value.map { book ->
            if (book.id.name == bookId) {
                book.copy(
                    isRead = isRead,
                    chapters = book.chapters.map { it.copy(isRead = isRead) },
                )
            } else {
                book
            }
        }
    }
}

private class BookStateChapterDao(
    private val booksRepository: FakeBooksRepository,
) : ThrowingChapterDao() {
    override suspend fun getChaptersByBookId(bookId: String): List<ChapterEntity> = booksRepository.books.value
        .filter { it.id.name == bookId }
        .flatMap { book ->
            book.chapters.map { chapter ->
                ChapterEntity(
                    id = chapter.number.toLong(),
                    number = chapter.number,
                    bookId = bookId,
                    isRead = chapter.isRead,
                )
            }
        }

    override suspend fun getChapterByBookIdAndNumber(
        bookId: String,
        chapterNumber: Int,
    ): ChapterEntity? = getChaptersByBookId(bookId).find { it.number == chapterNumber }

    override suspend fun updateChaptersReadStatusByBook(
        bookId: String,
        isRead: Boolean,
        updatedAt: Long,
    ) = Unit
}

private class BookStateVerseDao : ThrowingVerseDao() {
    override suspend fun getVersesByChapterId(chapterId: Long): List<VerseEntity> = emptyList()

    override suspend fun updateVersesReadStatusByBook(
        bookId: String,
        isRead: Boolean,
    ) = Unit
}
