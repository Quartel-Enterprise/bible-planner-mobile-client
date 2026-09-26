package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.GetBooksFlowUseCase
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
internal class ToggleChapterReadStatusUseCaseTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val passage = PassageModel(
        bookId = BookId.GEN,
        chapters = listOf(
            ChapterModel(
                number = 1,
                startVerse = null,
                endVerse = null,
                bookId = BookId.GEN,
            ),
        ),
        isRead = false,
        chapterRanges = "1",
    )
    private lateinit var useCase: ToggleChapterReadStatusUseCase
    private lateinit var database: InMemoryBibleDatabase
    private lateinit var dayRepository: FakeDayRepository

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN an unread chapter WHEN toggling it THEN reads it and returns the new status`() =
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
                passage = passage,
                readingPlanType = ReadingPlanType.BOOKS,
            )

            // Then
            assertEquals(true, result.getOrNull())
            val genesis = database.booksRepository
                .getBooksFlow()
                .first()
                .single()
            assertTrue(genesis.chapters.single().isRead)
            assertEquals(true, dayRepository.readStatusUpdates.single().isRead)
        }

    @Test
    fun `GIVEN an invalid chapter WHEN toggling it THEN fails without updating the day`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val result = useCase(
            weekNumber = 1,
            dayNumber = 1,
            strategy = UpdateReadStatusOfPassageStrategy.Chapter(
                passageIndex = 0,
                chapterIndex = 3,
            ),
            passage = passage,
            readingPlanType = ReadingPlanType.BOOKS,
        )

        // Then
        assertTrue(result.isFailure)
        assertTrue(dayRepository.readStatusUpdates.isEmpty())
    }

    private suspend fun prepareScenario() {
        database = InMemoryBibleDatabase(testDispatcher)
        database.insertBook(
            bookId = BookId.GEN,
            versesPerChapter = listOf(2),
        )
        dayRepository = FakeDayRepository(
            day = null,
            daysWithNotesCount = 0,
        )
        useCase = ToggleChapterReadStatusUseCase(
            calculateChapterReadStatus = IsChapterReadStatusUseCase(GetBooksFlowUseCase(database.booksRepository)),
            updateChapterReadStatus = UpdateChapterReadStatusUseCase(
                dayRepository = dayRepository,
                markPassagesRead = database.updatePassageReadStatus(
                    currentTimestampProvider = { 10L },
                    trackEvent = { _, _ -> },
                ),
                getPlansByWeek = GetPlansByWeekUseCase(
                    planRepository = FakePlanRepository(
                        plans = mapOf(
                            ReadingPlanType.BOOKS to listOf(
                                WeekPlanModel(
                                    number = 1,
                                    days = listOf(
                                        DayModel(
                                            number = 1,
                                            passages = listOf(passage),
                                            isRead = false,
                                            totalVerses = 0,
                                            readVerses = 0,
                                            readTimestamp = null,
                                            plannedReadDate = null,
                                            notes = null,
                                            isToday = false,
                                        ),
                                    ),
                                ),
                            ),
                        ),
                        startDate = null,
                    ),
                    booksRepository = database.booksRepository,
                    getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                    currentTimestampProvider = { 10L },
                    localDateTimeProvider = { LocalDateTime(LocalDate(2026, 1, 1), LocalTime(8, 0)) },
                ),
                currentTimestampProvider = { 10L },
            ),
        )
    }
}
