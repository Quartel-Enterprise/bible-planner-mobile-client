package com.quare.bibleplanner.feature.day.domain

import com.quare.bibleplanner.core.date.toTimestampUTC
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class EditDaySelectableDatesTest {
    private val today = LocalDate(
        year = 2026,
        month = 5,
        day = 10,
    )
    private val yesterday = LocalDate(
        year = 2026,
        month = 5,
        day = 9,
    )
    private val tomorrow = LocalDate(
        year = 2026,
        month = 5,
        day = 11,
    )
    private val newYearsEve = LocalDate(
        year = 2026,
        month = 12,
        day = 31,
    )
    private val newYearsDay = LocalDate(
        year = 2027,
        month = 1,
        day = 1,
    )
    private val eastOfUtc = TimeZone.of("Asia/Tokyo")
    private val westOfUtc = TimeZone.of("America/Sao_Paulo")
    private lateinit var selectableDates: EditDaySelectableDates

    @Test
    fun `GIVEN a past date WHEN checking THEN it is selectable`() {
        // Given
        prepareScenario(
            timeZone = westOfUtc,
            now = LocalDateTime(
                date = today,
                time = LocalTime(hour = 12, minute = 0),
            ),
        )

        // When
        val isSelectable = selectableDates.isSelectableDate(yesterday.toTimestampUTC())

        // Then
        assertTrue(isSelectable)
    }

    @Test
    fun `GIVEN a morning east of UTC WHEN checking today THEN it is selectable`() {
        // Given
        prepareScenario(
            timeZone = eastOfUtc,
            now = LocalDateTime(
                date = today,
                time = LocalTime(hour = 8, minute = 0),
            ),
        )

        // When
        val isSelectable = selectableDates.isSelectableDate(today.toTimestampUTC())

        // Then
        assertTrue(isSelectable)
    }

    @Test
    fun `GIVEN a late night west of UTC WHEN checking tomorrow THEN it is not selectable`() {
        // Given
        prepareScenario(
            timeZone = westOfUtc,
            now = LocalDateTime(
                date = today,
                time = LocalTime(hour = 22, minute = 0),
            ),
        )

        // When
        val isSelectable = selectableDates.isSelectableDate(tomorrow.toTimestampUTC())

        // Then
        assertFalse(isSelectable)
    }

    @Test
    fun `GIVEN a New Year morning east of UTC WHEN checking the new year THEN it is selectable`() {
        // Given
        prepareScenario(
            timeZone = eastOfUtc,
            now = LocalDateTime(
                date = newYearsDay,
                time = LocalTime(hour = 5, minute = 0),
            ),
        )

        // When
        val isSelectable = selectableDates.isSelectableYear(newYearsDay.year)

        // Then
        assertTrue(isSelectable)
    }

    @Test
    fun `GIVEN a New Year Eve night west of UTC WHEN checking next year THEN it is not selectable`() {
        // Given
        prepareScenario(
            timeZone = westOfUtc,
            now = LocalDateTime(
                date = newYearsEve,
                time = LocalTime(hour = 22, minute = 0),
            ),
        )

        // When
        val isSelectable = selectableDates.isSelectableYear(newYearsDay.year)

        // Then
        assertFalse(isSelectable)
    }

    private fun prepareScenario(
        timeZone: TimeZone,
        now: LocalDateTime,
    ) {
        selectableDates = EditDaySelectableDates(
            currentTimestampProvider = { now.toInstant(timeZone).toEpochMilliseconds() },
            localDateTimeProvider = { timestamp ->
                Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timeZone)
            },
        )
    }
}
