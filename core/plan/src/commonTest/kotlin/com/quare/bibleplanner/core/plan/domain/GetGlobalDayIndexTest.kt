package com.quare.bibleplanner.core.plan.domain

import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetGlobalDayIndexTest {
    @Test
    fun `GIVEN days across weeks WHEN computing their global index THEN counts seven days per earlier week`() {
        // When
        val indexes = listOf(1 to 1, 1 to 7, 2 to 1, 52 to 7).map { (week, day) ->
            getGlobalDayIndex(
                weekNumber = week,
                dayNumber = day,
            )
        }

        // Then
        assertEquals(listOf(1, 7, 8, 364), indexes)
    }
}
