package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.fake.FakeBooksRepository
import com.quare.bibleplanner.core.plan.fake.FakePlanRepository
import com.quare.bibleplanner.core.plan.fake.book
import com.quare.bibleplanner.core.plan.fake.bookChapter
import com.quare.bibleplanner.core.plan.fake.day
import com.quare.bibleplanner.core.plan.fake.passage
import com.quare.bibleplanner.core.plan.fake.week
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetCompletedDayForChapterUseCaseTest {
    private val weeks = listOf(
        week(
            number = 4,
            days = listOf(
                day(
                    number = 1,
                    passages = listOf(passage(bookId = BookId.JUD)),
                ),
                day(
                    number = 2,
                    passages = listOf(passage(bookId = BookId.OBA)),
                ),
            ),
        ),
    )

    private lateinit var useCase: GetCompletedDayForChapterUseCase

    @Test
    fun `GIVEN the selected plan schedules a now read day WHEN looking up the chapter THEN returns that day`() =
        runTest {
            // Given
            prepareScenario(selectedReadingPlan = ReadingPlanType.BOOKS)

            // When
            val result = useCase(
                bookId = BookId.OBA,
                chapterNumber = 1,
            )

            // Then
            assertEquals(
                PlanDayLocationModel(
                    weekNumber = 4,
                    dayNumber = 2,
                    readingPlanType = ReadingPlanType.BOOKS,
                ),
                result,
            )
        }

    @Test
    fun `GIVEN the chapter day is still unread WHEN looking up the chapter THEN returns null`() = runTest {
        // Given
        prepareScenario(selectedReadingPlan = ReadingPlanType.BOOKS)

        // When
        val result = useCase(
            bookId = BookId.JUD,
            chapterNumber = 1,
        )

        // Then
        assertNull(result)
    }

    @Test
    fun `GIVEN another plan is selected WHEN looking up the chapter THEN ignores the plan it is read in`() = runTest {
        // Given
        prepareScenario(selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL)

        // When
        val result = useCase(
            bookId = BookId.OBA,
            chapterNumber = 1,
        )

        // Then
        assertNull(result)
    }

    private fun prepareScenario(selectedReadingPlan: ReadingPlanType) {
        val planRepository = FakePlanRepository(
            plans = mapOf(ReadingPlanType.BOOKS to weeks),
            startDate = null,
            selectedReadingPlan = selectedReadingPlan,
        )
        useCase = GetCompletedDayForChapterUseCase(
            getPlansByWeekUseCase = GetPlansByWeekUseCase(
                planRepository = planRepository,
                booksRepository = FakeBooksRepository(
                    listOf(
                        book(
                            bookId = BookId.OBA,
                            chapters = listOf(
                                bookChapter(
                                    number = 1,
                                    verseCount = 21,
                                    isRead = true,
                                ),
                            ),
                            isRead = true,
                        ),
                    ),
                ),
                getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                currentTimestampProvider = CurrentTimestampProvider { 0L },
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
            planRepository = planRepository,
        )
    }
}
