package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import com.quare.bibleplanner.feature.day.fake.FakeDayRepository
import com.quare.bibleplanner.feature.day.fake.FakePlanRepository
import com.quare.bibleplanner.feature.day.fake.InMemoryBibleDatabase
import com.quare.bibleplanner.feature.day.fake.ReadStatusUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class UpdateChapterReadStatusUseCaseTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val readTimestamp = 1_700_000_000_000L
    private val planDay = DayModel(
        number = 1,
        passages = listOf(
            PassageModel(
                bookId = BookId.GEN,
                chapters = listOf(chapter(number = 1), chapter(number = 2)),
                isRead = false,
                chapterRanges = "1-2",
            ),
            PassageModel(
                bookId = BookId.OBA,
                chapters = emptyList(),
                isRead = false,
                chapterRanges = null,
            ),
        ),
        isRead = false,
        totalVerses = 0,
        readVerses = 0,
        readTimestamp = null,
        plannedReadDate = null,
        notes = null,
        isToday = false,
    )
    private lateinit var useCase: UpdateChapterReadStatusUseCase
    private lateinit var database: InMemoryBibleDatabase
    private lateinit var dayRepository: FakeDayRepository

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN an unread chapter WHEN marking it THEN reads it and keeps the unfinished day unread`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            val result = useCase(
                weekNumber = 1,
                dayNumber = 1,
                strategy = UpdateReadStatusOfPassageStrategy.Chapter(
                    passageIndex = 0,
                    chapterIndex = 0,
                ),
                isRead = true,
                readingPlanType = ReadingPlanType.BOOKS,
            )

            // Then
            assertTrue(result.isSuccess)
            assertEquals(listOf(true, false), book(BookId.GEN).chapters.map { it.isRead })
            assertEquals(
                listOf(
                    dayUpdate(
                        isRead = false,
                        readTimestamp = null,
                    ),
                ),
                dayRepository.readStatusUpdates,
            )
        }

    @Test
    fun `GIVEN an unread whole book passage WHEN marking the entire book THEN reads every chapter of it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            val result = useCase(
                weekNumber = 1,
                dayNumber = 1,
                strategy = UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex = 1),
                isRead = true,
                readingPlanType = ReadingPlanType.BOOKS,
            )

            // Then
            assertTrue(result.isSuccess)
            assertTrue(book(BookId.OBA).isRead)
            assertEquals(listOf(false, false), book(BookId.GEN).chapters.map { it.isRead })
        }

    @Test
    fun `GIVEN only one unread chapter left WHEN marking it THEN marks the whole day read now`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            listOf(
                UpdateReadStatusOfPassageStrategy.Chapter(
                    passageIndex = 0,
                    chapterIndex = 0,
                ),
                UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex = 1),
            ).forEach { strategy ->
                useCase(
                    weekNumber = 1,
                    dayNumber = 1,
                    strategy = strategy,
                    isRead = true,
                    readingPlanType = ReadingPlanType.BOOKS,
                )
            }

            // When
            useCase(
                weekNumber = 1,
                dayNumber = 1,
                strategy = UpdateReadStatusOfPassageStrategy.Chapter(
                    passageIndex = 0,
                    chapterIndex = 1,
                ),
                isRead = true,
                readingPlanType = ReadingPlanType.BOOKS,
            )

            // Then
            assertEquals(
                dayUpdate(
                    isRead = true,
                    readTimestamp = readTimestamp,
                ),
                dayRepository.readStatusUpdates.last(),
            )
        }

    @Test
    fun `GIVEN the chronological plan WHEN marking a chapter THEN reads the day from that plan`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(planType = ReadingPlanType.CHRONOLOGICAL)

            // When
            val result = useCase(
                weekNumber = 1,
                dayNumber = 1,
                strategy = UpdateReadStatusOfPassageStrategy.Chapter(
                    passageIndex = 0,
                    chapterIndex = 1,
                ),
                isRead = true,
                readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            )

            // Then
            assertTrue(result.isSuccess)
            assertEquals(listOf(false, true), book(BookId.GEN).chapters.map { it.isRead })
            assertEquals(ReadingPlanType.CHRONOLOGICAL, dayRepository.readStatusUpdates.single().readingPlanType)
        }

    @Test
    fun `GIVEN a location outside the plan WHEN marking THEN fails without touching anything`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val invalidRequests = listOf(
                Triple(9, 1, UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex = 0)),
                Triple(1, 9, UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex = 0)),
                Triple(1, 1, UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex = 5)),
                Triple(1, 1, UpdateReadStatusOfPassageStrategy.EntireBook(passageIndex = -1)),
                Triple(
                    1,
                    1,
                    UpdateReadStatusOfPassageStrategy.Chapter(
                        passageIndex = 0,
                        chapterIndex = 2,
                    ),
                ),
                Triple(
                    1,
                    1,
                    UpdateReadStatusOfPassageStrategy.Chapter(
                        passageIndex = 0,
                        chapterIndex = -1,
                    ),
                ),
            )

            // When
            val results = invalidRequests.map { (weekNumber, dayNumber, strategy) ->
                useCase(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                    strategy = strategy,
                    isRead = true,
                    readingPlanType = ReadingPlanType.BOOKS,
                )
            }

            // Then
            assertTrue(results.all(Result<Unit>::isFailure))
            assertTrue(dayRepository.readStatusUpdates.isEmpty())
            assertEquals(listOf(false, false), book(BookId.GEN).chapters.map { it.isRead })
        }

    private suspend fun book(bookId: BookId): BookDataModel = database.booksRepository
        .getBooksFlow()
        .first()
        .single { it.id == bookId }

    private fun chapter(number: Int): ChapterModel = ChapterModel(
        number = number,
        startVerse = null,
        endVerse = null,
        bookId = BookId.GEN,
    )

    private fun dayUpdate(
        isRead: Boolean,
        readTimestamp: Long?,
    ): ReadStatusUpdate = ReadStatusUpdate(
        weekNumber = 1,
        dayNumber = 1,
        readingPlanType = ReadingPlanType.BOOKS,
        isRead = isRead,
        readTimestamp = readTimestamp,
    )

    private suspend fun TestScope.prepareScenario(planType: ReadingPlanType = ReadingPlanType.BOOKS) {
        database = InMemoryBibleDatabase(testDispatcher)
        database.insertBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(3, 2),
        )
        database.insertBook(
            bookId = BookId.OBA,
            versesPerChapter = listOf(2),
        )
        dayRepository = FakeDayRepository(
            day = null,
            daysWithNotesCount = 0,
        )
        useCase = UpdateChapterReadStatusUseCase(
            dayRepository = dayRepository,
            markPassagesRead = database.updatePassageReadStatus(
                currentTimestampProvider = { readTimestamp },
                trackEvent = { _, _ -> },
            ),
            getPlansByWeek = GetPlansByWeekUseCase(
                planRepository = FakePlanRepository(
                    plans = mapOf(
                        planType to listOf(
                            WeekPlanModel(
                                number = 1,
                                days = listOf(planDay),
                            ),
                        ),
                    ),
                    startDate = null,
                ),
                booksRepository = database.booksRepository,
                getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                currentTimestampProvider = { readTimestamp },
                localDateTimeProvider = { LocalDateTime(LocalDate(2026, 1, 1), LocalTime(8, 0)) },
            ),
            currentTimestampProvider = { readTimestamp },
        )
    }
}
