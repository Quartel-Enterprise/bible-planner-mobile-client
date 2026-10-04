package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ResolvePlanStatusUseCaseTest {
    private val useCase = ResolvePlanStatusUseCase()

    @Test
    fun `GIVEN no reads and today as the first day WHEN resolving THEN returns New`() {
        // Given
        val weeks = singleWeek(
            day(number = 1, isToday = true),
            day(number = 2),
            day(number = 3),
        )

        // When
        val status = useCase(weeks)

        // Then
        assertEquals(PlanMode.New, status.mode)
        assertEquals(0, status.readDays)
        assertEquals(1, status.nextDay?.globalIndex)
    }

    @Test
    fun `GIVEN the next day equal to today WHEN resolving THEN returns OnTrack`() {
        // Given
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
            day(number = 3, isRead = true),
            day(number = 4, isToday = true),
            day(number = 5),
        )

        // When
        val status = useCase(weeks)

        // Then
        assertEquals(PlanMode.OnTrack, status.mode)
        assertEquals(3, status.readDays)
        assertEquals(4, status.nextDay?.globalIndex)
    }

    @Test
    fun `GIVEN reads through today and the next day tomorrow WHEN resolving THEN returns CaughtUp`() {
        // Given
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
            day(number = 3, isToday = true, isRead = true),
            day(number = 4),
        )

        // When
        val status = useCase(weeks)

        // Then
        assertEquals(PlanMode.CaughtUp, status.mode)
        assertEquals(4, status.nextDay?.globalIndex)
    }

    @Test
    fun `GIVEN reads past today WHEN resolving THEN returns Ahead with daysAhead`() {
        // Given
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
            day(number = 3, isToday = true, isRead = true),
            day(number = 4, isRead = true),
            day(number = 5, isRead = true),
            day(number = 6),
        )

        // When
        val status = useCase(weeks)

        // Then
        assertEquals(PlanMode.Ahead, status.mode)
        assertEquals(2, status.daysAhead)
        assertEquals(6, status.nextDay?.globalIndex)
    }

    @Test
    fun `GIVEN the next day before today WHEN resolving THEN returns Behind with daysBehind and lapse`() {
        // Given
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
            day(number = 3),
            day(number = 4),
            day(number = 5, isToday = true),
        )

        // When
        val status = useCase(weeks)

        // Then
        assertEquals(PlanMode.Behind, status.mode)
        assertEquals(2, status.daysBehind)
        assertEquals(3, status.daysSinceLastRead)
        assertEquals(3, status.nextDay?.globalIndex)
        assertEquals(5, status.todayDay?.globalIndex)
    }

    @Test
    fun `GIVEN a behind plan read on or past today WHEN resolving THEN has a null lapse`() {
        // Given
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2),
            day(number = 3, isToday = true, isRead = true),
        )

        // When
        val status = useCase(weeks)

        // Then
        assertEquals(PlanMode.Behind, status.mode)
        assertEquals(1, status.daysBehind)
        assertNull(status.daysSinceLastRead)
    }

    @Test
    fun `GIVEN all days read WHEN resolving THEN returns Done`() {
        // Given
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
        )

        // When
        val status = useCase(weeks)

        // Then
        assertEquals(PlanMode.Done, status.mode)
        assertNull(status.nextDay)
    }

    @Test
    fun `GIVEN several weeks WHEN resolving THEN the global index spans the weeks`() {
        // Given
        val weeks = listOf(
            WeekPlanModel(number = 1, days = (1..7).map { day(number = it, isRead = true) }),
            WeekPlanModel(number = 2, days = listOf(day(number = 1, isToday = true))),
        )

        // When
        val status = useCase(weeks)

        // Then
        assertEquals(PlanMode.OnTrack, status.mode)
        assertEquals(8, status.nextDay?.globalIndex)
        assertEquals(8, status.todayDay?.globalIndex)
        assertEquals(8, status.totalDays)
    }

    @Test
    fun `GIVEN consecutive reads WHEN resolving THEN the streak counts them up to the first unread day`() {
        // Given
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
            day(number = 3, isToday = true),
        )

        // When
        val status = useCase(weeks)

        // Then
        assertEquals(2, status.streakDays)
    }

    @Test
    fun `GIVEN reads after a gap WHEN resolving THEN the streak ignores them`() {
        // Given
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2),
            day(number = 3, isRead = true),
            day(number = 4, isRead = true),
        )

        // When
        val status = useCase(weeks)

        // Then
        assertEquals(1, status.streakDays)
    }

    private fun singleWeek(vararg days: DayModel): List<WeekPlanModel> =
        listOf(WeekPlanModel(number = 1, days = days.toList()))
}
