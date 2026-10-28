package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.books.testing.FakeBooksRepository
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.fake.book
import com.quare.bibleplanner.core.plan.fake.bookChapter
import com.quare.bibleplanner.core.plan.fake.chapterPlan
import com.quare.bibleplanner.core.plan.fake.day
import com.quare.bibleplanner.core.plan.fake.passage
import com.quare.bibleplanner.core.plan.fake.week
import com.quare.bibleplanner.core.plan.testing.FakePlanRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetReadingStreakUseCaseTest {
    // Genesis 1, 2 and 4 are read; Genesis 3 is not.
    private val books = listOf(
        book(
            bookId = BookId.GEN,
            chapters = listOf(
                bookChapter(
                    number = 1,
                    verseCount = 2,
                    isRead = true,
                ),
                bookChapter(
                    number = 2,
                    verseCount = 2,
                    isRead = true,
                ),
                bookChapter(
                    number = 3,
                    verseCount = 2,
                ),
                bookChapter(
                    number = 4,
                    verseCount = 2,
                    isRead = true,
                ),
            ),
        ),
    )

    // Read, then (next week) read, unread, read ahead: a streak of 2 that crosses the week boundary.
    private val chronologicalWeeks = listOf(
        week(
            number = 1,
            days = listOf(
                genesisDay(
                    number = 1,
                    chapter = 1,
                ),
            ),
        ),
        week(
            number = 2,
            days = listOf(
                genesisDay(
                    number = 1,
                    chapter = 2,
                ),
                genesisDay(
                    number = 2,
                    chapter = 3,
                ),
                genesisDay(
                    number = 3,
                    chapter = 4,
                ),
            ),
        ),
    )

    // Read, unread, read ahead: a streak of 1.
    private val booksOrderWeeks = listOf(
        week(
            number = 1,
            days = listOf(
                genesisDay(
                    number = 1,
                    chapter = 4,
                ),
                genesisDay(
                    number = 2,
                    chapter = 3,
                ),
                genesisDay(
                    number = 3,
                    chapter = 1,
                ),
            ),
        ),
    )

    private lateinit var planRepository: FakePlanRepository
    private lateinit var useCase: GetReadingStreakUseCase

    @Test
    fun `GIVEN the chronological plan WHEN getting the streak THEN counts read days across weeks up to a gap`() =
        runTest {
            // Given
            prepareScenario(selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL)

            // When
            val streak = useCase()

            // Then
            assertEquals(2, streak)
        }

    @Test
    fun `GIVEN the books order plan WHEN getting the streak THEN returns the streak of that plan only`() = runTest {
        // Given
        prepareScenario(selectedReadingPlan = ReadingPlanType.BOOKS)

        // When
        val streak = useCase()

        // Then
        assertEquals(1, streak)
    }

    @Test
    fun `GIVEN the user switched to the books order plan WHEN getting the streak THEN uses the newly selected plan`() =
        runTest {
            // Given
            prepareScenario(selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL)
            planRepository.setSelectedReadingPlan(ReadingPlanType.BOOKS)

            // When
            val streak = useCase()

            // Then
            assertEquals(1, streak)
        }

    @Test
    fun `GIVEN the selected plan has no days WHEN getting the streak THEN ignores the other plan and returns zero`() =
        runTest {
            // Given
            prepareScenario(
                selectedReadingPlan = ReadingPlanType.BOOKS,
                plans = mapOf(ReadingPlanType.CHRONOLOGICAL to chronologicalWeeks),
            )

            // When
            val streak = useCase()

            // Then
            assertEquals(0, streak)
        }

    @Test
    fun `GIVEN the selected plan is finished WHEN getting the streak THEN returns zero like the plan screen`() =
        runTest {
            // Given
            prepareScenario(
                selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
                plans = mapOf(
                    ReadingPlanType.CHRONOLOGICAL to listOf(
                        week(
                            number = 1,
                            days = listOf(
                                genesisDay(
                                    number = 1,
                                    chapter = 1,
                                ),
                                genesisDay(
                                    number = 2,
                                    chapter = 2,
                                ),
                            ),
                        ),
                        week(
                            number = 2,
                            days = listOf(
                                genesisDay(
                                    number = 1,
                                    chapter = 4,
                                ),
                            ),
                        ),
                    ),
                    ReadingPlanType.BOOKS to booksOrderWeeks,
                ),
            )

            // When
            val streak = useCase()

            // Then
            assertEquals(0, streak)
        }

    @Test
    fun `GIVEN the first day of the selected plan is unread WHEN getting the streak THEN returns zero`() = runTest {
        // Given
        prepareScenario(
            selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
            plans = mapOf(
                ReadingPlanType.CHRONOLOGICAL to listOf(
                    week(
                        number = 1,
                        days = listOf(
                            genesisDay(
                                number = 1,
                                chapter = 3,
                            ),
                            genesisDay(
                                number = 2,
                                chapter = 1,
                            ),
                        ),
                    ),
                ),
                ReadingPlanType.BOOKS to booksOrderWeeks,
            ),
        )

        // When
        val streak = useCase()

        // Then
        assertEquals(0, streak)
    }

    private fun genesisDay(
        number: Int,
        chapter: Int,
    ): DayModel = day(
        number = number,
        passages = listOf(
            passage(
                bookId = BookId.GEN,
                chapters = listOf(
                    chapterPlan(
                        bookId = BookId.GEN,
                        number = chapter,
                    ),
                ),
            ),
        ),
    )

    private fun prepareScenario(
        selectedReadingPlan: ReadingPlanType,
        plans: Map<ReadingPlanType, List<WeekPlanModel>> = mapOf(
            ReadingPlanType.CHRONOLOGICAL to chronologicalWeeks,
            ReadingPlanType.BOOKS to booksOrderWeeks,
        ),
    ) {
        planRepository = FakePlanRepository(
            plans = plans,
            startDate = null,
            selectedReadingPlan = selectedReadingPlan,
        )
        useCase = GetReadingStreakUseCase(
            getPlansByWeekUseCase = GetPlansByWeekUseCase(
                planRepository = planRepository,
                booksRepository = FakeBooksRepository(books),
                getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                currentTimestampProvider = CurrentTimestampProvider { 0L },
                localDateTimeProvider = LocalDateTimeProvider {
                    LocalDateTime(
                        date = LocalDate(
                            year = 2026,
                            month = 1,
                            day = 10,
                        ),
                        time = LocalTime(
                            hour = 8,
                            minute = 0,
                        ),
                    )
                },
            ),
            planRepository = planRepository,
        )
    }
}
