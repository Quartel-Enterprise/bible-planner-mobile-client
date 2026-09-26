package com.quare.bibleplanner.feature.day.domain

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

internal class EditDaySelectableDatesTest {
    private lateinit var selectableDates: EditDaySelectableDates

    @BeforeTest
    fun setUp() {
        selectableDates = EditDaySelectableDates()
    }

    @Test
    fun `GIVEN a past date WHEN checking THEN it is selectable`() {
        // Given
        val yesterday = (Clock.System.now() - 1.days).toEpochMilliseconds()

        // When
        val isSelectable = selectableDates.isSelectableDate(yesterday)

        // Then
        assertTrue(isSelectable)
    }

    @Test
    fun `GIVEN a future date WHEN checking THEN it is not selectable`() {
        // Given
        val tomorrow = (Clock.System.now() + 1.days).toEpochMilliseconds()

        // When
        val isSelectable = selectableDates.isSelectableDate(tomorrow)

        // Then
        assertFalse(isSelectable)
    }

    @Test
    fun `GIVEN the current and a past year WHEN checking THEN both are selectable`() {
        // Given
        val currentYear = Clock.System
            .now()
            .toLocalDateTime(TimeZone.UTC)
            .year

        // When
        val areSelectable = listOf(currentYear, currentYear - 1).map(selectableDates::isSelectableYear)

        // Then
        assertTrue(areSelectable.all { it })
    }

    @Test
    fun `GIVEN next year WHEN checking THEN it is not selectable`() {
        // Given
        val nextYear = Clock.System
            .now()
            .toLocalDateTime(TimeZone.UTC)
            .year + 1

        // When
        val isSelectable = selectableDates.isSelectableYear(nextYear)

        // Then
        assertFalse(isSelectable)
    }
}
