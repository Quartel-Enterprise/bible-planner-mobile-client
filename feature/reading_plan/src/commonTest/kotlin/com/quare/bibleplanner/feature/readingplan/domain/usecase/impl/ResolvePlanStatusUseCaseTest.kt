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
    fun `no reads and today is first day returns New`() {
        val weeks = singleWeek(
            day(number = 1, isToday = true),
            day(number = 2),
            day(number = 3),
        )
        val status = useCase(weeks)
        assertEquals(PlanMode.New, status.mode)
        assertEquals(0, status.readDays)
        assertEquals(1, status.nextDay?.globalIndex)
    }

    @Test
    fun `next equals today returns OnTrack`() {
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
            day(number = 3, isRead = true),
            day(number = 4, isToday = true),
            day(number = 5),
        )
        val status = useCase(weeks)
        assertEquals(PlanMode.OnTrack, status.mode)
        assertEquals(3, status.readDays)
        assertEquals(4, status.nextDay?.globalIndex)
    }

    @Test
    fun `read through today with next tomorrow returns CaughtUp`() {
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
            day(number = 3, isToday = true, isRead = true),
            day(number = 4),
        )
        val status = useCase(weeks)
        assertEquals(PlanMode.CaughtUp, status.mode)
        assertEquals(4, status.nextDay?.globalIndex)
    }

    @Test
    fun `read past today returns Ahead with daysAhead`() {
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
            day(number = 3, isToday = true, isRead = true),
            day(number = 4, isRead = true),
            day(number = 5, isRead = true),
            day(number = 6),
        )
        val status = useCase(weeks)
        assertEquals(PlanMode.Ahead, status.mode)
        assertEquals(2, status.daysAhead)
        assertEquals(6, status.nextDay?.globalIndex)
    }

    @Test
    fun `next before today returns Behind with daysBehind and lapse`() {
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
            day(number = 3),
            day(number = 4),
            day(number = 5, isToday = true),
        )
        val status = useCase(weeks)
        assertEquals(PlanMode.Behind, status.mode)
        assertEquals(2, status.daysBehind)
        assertEquals(3, status.daysSinceLastRead)
        assertEquals(3, status.nextDay?.globalIndex)
        assertEquals(5, status.todayDay?.globalIndex)
    }

    @Test
    fun `behind but read on or past today has null lapse`() {
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2),
            day(number = 3, isToday = true, isRead = true),
        )
        val status = useCase(weeks)
        assertEquals(PlanMode.Behind, status.mode)
        assertEquals(1, status.daysBehind)
        assertNull(status.daysSinceLastRead)
    }

    @Test
    fun `all days read returns Done`() {
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
        )
        val status = useCase(weeks)
        assertEquals(PlanMode.Done, status.mode)
        assertNull(status.nextDay)
    }

    @Test
    fun `global index spans weeks`() {
        val weeks = listOf(
            WeekPlanModel(number = 1, days = (1..7).map { day(number = it, isRead = true) }),
            WeekPlanModel(number = 2, days = listOf(day(number = 1, isToday = true))),
        )
        val status = useCase(weeks)
        assertEquals(PlanMode.OnTrack, status.mode)
        assertEquals(8, status.nextDay?.globalIndex)
        assertEquals(8, status.todayDay?.globalIndex)
        assertEquals(8, status.totalDays)
    }

    @Test
    fun `streak counts consecutive read days ending at first unread`() {
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2, isRead = true),
            day(number = 3, isToday = true),
        )
        val status = useCase(weeks)
        assertEquals(2, status.streakDays)
    }

    @Test
    fun `streak ignores reads after a gap`() {
        val weeks = singleWeek(
            day(number = 1, isRead = true),
            day(number = 2),
            day(number = 3, isRead = true),
            day(number = 4, isRead = true),
        )
        val status = useCase(weeks)
        assertEquals(1, status.streakDays)
    }

    private fun singleWeek(vararg days: DayModel): List<WeekPlanModel> =
        listOf(WeekPlanModel(number = 1, days = days.toList()))
}
