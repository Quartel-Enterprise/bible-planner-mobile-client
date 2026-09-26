package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.ScheduledDayModel
import com.quare.bibleplanner.core.plan.fake.FakePlanRepository
import com.quare.bibleplanner.core.plan.fake.day
import com.quare.bibleplanner.core.plan.fake.passage
import com.quare.bibleplanner.core.plan.fake.week
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetScheduledDayUseCaseTest {
    private val passages = listOf(passage(bookId = BookId.OBA))

    private lateinit var useCase: GetScheduledDayUseCase

    @Test
    fun `GIVEN a start date WHEN getting a scheduled day THEN returns its passages and due date`() = runTest {
        // Given
        prepareScenario(
            startDate = LocalDate(
                year = 2026,
                month = 2,
                day = 1,
            ),
        )

        // When
        val result = useCase(
            weekNumber = 1,
            dayNumber = 2,
            readingPlanType = ReadingPlanType.BOOKS,
        )

        // Then
        assertEquals(
            ScheduledDayModel(
                number = 2,
                passages = passages,
                plannedReadDate = LocalDate(
                    year = 2026,
                    month = 2,
                    day = 2,
                ),
            ),
            result,
        )
    }

    @Test
    fun `GIVEN no start date WHEN getting a scheduled day THEN leaves it without due date`() = runTest {
        // Given
        prepareScenario(startDate = null)

        // When
        val result = useCase(
            weekNumber = 1,
            dayNumber = 2,
            readingPlanType = ReadingPlanType.BOOKS,
        )

        // Then
        assertNull(result?.plannedReadDate)
        assertEquals(passages, result?.passages)
    }

    @Test
    fun `GIVEN a missing day or week WHEN getting a scheduled day THEN returns null`() = runTest {
        // Given
        prepareScenario(startDate = null)

        // When
        val missingDay = useCase(
            weekNumber = 1,
            dayNumber = 7,
            readingPlanType = ReadingPlanType.BOOKS,
        )
        val missingWeek = useCase(
            weekNumber = 9,
            dayNumber = 2,
            readingPlanType = ReadingPlanType.BOOKS,
        )

        // Then
        assertNull(missingDay)
        assertNull(missingWeek)
    }

    private fun prepareScenario(startDate: LocalDate?) {
        useCase = GetScheduledDayUseCase(
            planRepository = FakePlanRepository(
                plans = mapOf(
                    ReadingPlanType.BOOKS to listOf(
                        week(
                            number = 1,
                            days = listOf(
                                day(
                                    number = 2,
                                    passages = passages,
                                ),
                            ),
                        ),
                    ),
                ),
                startDate = startDate,
                selectedReadingPlan = ReadingPlanType.BOOKS,
            ),
            getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
        )
    }
}
