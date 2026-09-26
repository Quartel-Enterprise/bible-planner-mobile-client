package com.quare.bibleplanner.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SharedTransitionAnimationUtilsTest {
    @Test
    fun `GIVEN a week WHEN building its shared element ids THEN derives the separator from the week id`() {
        // When
        val weekId = SharedTransitionAnimationUtils.buildWeekNumberId(3)
        val separatorId = SharedTransitionAnimationUtils.buildWeekSeparatorId(3)

        // Then
        assertEquals("week_number_3", weekId)
        assertEquals("week_number_3_separator", separatorId)
    }

    @Test
    fun `GIVEN a day WHEN building its shared element ids THEN each element gets its own id`() {
        // When
        val ids = with(SharedTransitionAnimationUtils) {
            listOf(
                buildDayNumberId(
                    weekNumber = 3,
                    dayNumebr = 5,
                ),
                buildPlannedDay(
                    weekNumber = 3,
                    dayNumber = 5,
                ),
                buildPlannedMonth(
                    weekNumber = 3,
                    dayNumber = 5,
                ),
                buildPlannedYear(
                    weekNumber = 3,
                    dayNumber = 5,
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
