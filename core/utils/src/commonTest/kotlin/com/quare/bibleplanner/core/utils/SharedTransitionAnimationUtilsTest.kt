package com.quare.bibleplanner.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SharedTransitionAnimationUtilsTest {
    @Test
    fun `GIVEN a week WHEN building its shared element ids THEN derives the separator from the week id`() {
        // Given
        val weekNumber = 3

        // When
        val weekId = SharedTransitionAnimationUtils.buildWeekNumberId(weekNumber)
        val separatorId = SharedTransitionAnimationUtils.buildWeekSeparatorId(weekNumber)

        // Then
        assertEquals("week_number_3", weekId)
        assertEquals("week_number_3_separator", separatorId)
    }

    @Test
    fun `GIVEN a day WHEN building its shared element ids THEN each element gets its own id`() {
        // Given
        val weekNumber = 3
        val dayNumber = 5

        // When
        val ids = with(SharedTransitionAnimationUtils) {
            listOf(
                buildDayNumberId(
                    weekNumber = weekNumber,
                    dayNumebr = dayNumber,
                ),
                buildPlannedDay(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                ),
                buildPlannedMonth(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                ),
                buildPlannedYear(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                ),
            )
        }

        // Then
        assertEquals(
            listOf(
                "week_number_3_day_5",
                "planned_day_3_5",
                "planned_month_3_5",
                "planned_year_3_5",
            ),
            ids,
        )
    }
}
