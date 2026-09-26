package com.quare.bibleplanner.feature.readingplan.presentation.mapper

import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.feature.readingplan.domain.usecase.impl.day
import com.quare.bibleplanner.feature.readingplan.presentation.model.DayPlanPresentationModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekGroup
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class WeeksPlanPresentationMapperTest {
    private lateinit var mapper: WeeksPlanPresentationMapper

    @BeforeTest
    fun setUp() {
        mapper = WeeksPlanPresentationMapper(
            localDateTimeProvider = {
                LocalDateTime(
                    year = 2026,
                    month = 3,
                    day = 10,
                    hour = 12,
                    minute = 0,
                )
            },
            currentTimestampProvider = { 0L },
        )
    }

    @Test
    fun `GIVEN read and unread weeks WHEN mapping THEN groups them into completed current and upcoming`() {
        // Given
        val weeks = listOf(
            week(number = 1, readDays = 2),
            week(number = 2, readDays = 1),
            week(number = 3, readDays = 0),
        )

        // When
        val result = mapper.map(
            weeks = weeks,
            expandedWeeks = setOf(2),
        )

        // Then
        assertEquals(
            expected = listOf(WeekGroup.Completed, WeekGroup.Current, WeekGroup.Upcoming),
            actual = result.map(WeekPlanPresentationModel::group),
        )
        assertEquals(
            expected = listOf(false, true, false),
            actual = result.map(WeekPlanPresentationModel::isExpanded),
        )
        assertEquals(
            expected = listOf(2, 1, 0),
            actual = result.map(WeekPlanPresentationModel::readDaysCount),
        )
        assertEquals(
            expected = listOf(2, 2, 2),
            actual = result.map(WeekPlanPresentationModel::totalDays),
        )
    }

    @Test
    fun `GIVEN a partly read plan WHEN mapping THEN accents only the next day to read`() {
        // Given
        val weeks = listOf(week(number = 1, readDays = 1))

        // When
        val result = mapper.map(
            weeks = weeks,
            expandedWeeks = emptySet(),
        )

        // Then
        val dayPlans = result.single().dayPlans
        assertEquals(
            expected = listOf(false, true),
            actual = dayPlans.map(DayPlanPresentationModel::isNextToRead),
        )
        assertEquals(
            expected = listOf(false, true),
            actual = dayPlans.map(DayPlanPresentationModel::isAccented),
        )
        assertEquals(
            expected = listOf(1, 2),
            actual = dayPlans.map(DayPlanPresentationModel::globalDayIndex),
        )
    }

    @Test
    fun `GIVEN unread days before today WHEN mapping THEN marks them overdue and today as active`() {
        // Given
        val weeks = listOf(
            WeekPlanModel(
                number = 1,
                days = listOf(
                    day(number = 1, isRead = true),
                    day(number = 2),
                    day(number = 3, isToday = true),
                    day(number = 4),
                ),
            ),
        )

        // When
        val result = mapper.map(
            weeks = weeks,
            expandedWeeks = emptySet(),
        )

        // Then
        val dayPlans = result.single().dayPlans
        assertEquals(
            expected = listOf(false, true, false, false),
            actual = dayPlans.map(DayPlanPresentationModel::isOverdue),
        )
        assertEquals(
            expected = listOf(false, false, true, false),
            actual = dayPlans.map(DayPlanPresentationModel::isActive),
        )
        assertTrue(dayPlans[2].isAccented)
    }

    @Test
    fun `GIVEN a read day planned for today WHEN mapping THEN it is not active`() {
        // Given
        val weeks = listOf(
            WeekPlanModel(
                number = 1,
                days = listOf(day(number = 1, isRead = true, isToday = true)),
            ),
        )

        // When
        val result = mapper.map(
            weeks = weeks,
            expandedWeeks = emptySet(),
        )

        // Then
        assertFalse(
            result
                .single()
                .dayPlans
                .single()
                .isActive,
        )
    }

    @Test
    fun `GIVEN days planned in several years WHEN mapping THEN shows the year only outside the current one`() {
        // Given
        val weeks = listOf(
            WeekPlanModel(
                number = 1,
                days = listOf(
                    day(number = 1, plannedReadDate = LocalDate(year = 2026, month = 12, day = 31)),
                    day(number = 2, plannedReadDate = LocalDate(year = 2027, month = 1, day = 1)),
                ),
            ),
        )

        // When
        val result = mapper.map(
            weeks = weeks,
            expandedWeeks = emptySet(),
        )

        // Then
        assertEquals(
            expected = listOf(false, true),
            actual = result.single().dayPlans.map(DayPlanPresentationModel::shouldShowYear),
        )
    }

    private fun week(
        number: Int,
        readDays: Int,
    ): WeekPlanModel = WeekPlanModel(
        number = number,
        days = (1..2).map { dayNumber ->
            day(
                number = dayNumber,
                isRead = dayNumber <= readDays,
            )
        },
    )
}
