package com.quare.bibleplanner.core.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

internal class LocalDateTimeConversionsTest {
    private val date = LocalDate(
        year = 2024,
        month = 3,
        day = 15,
    )

    @Test
    fun `GIVEN a timestamp WHEN converting it to local date time THEN converts back to the same instant`() {
        // Given
        val timestamp = 1_710_504_000_000L

        // When
        val localDateTime = LocalDateTimeProviderImpl().getLocalDateTime(timestamp)

        // Then
        assertEquals(timestamp, localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
    }

    @Test
    fun `GIVEN a local date time WHEN keeping only the date THEN drops the time`() {
        // Given
        val localDateTime = LocalDateTime(
            date = date,
            time = LocalTime(
                hour = 23,
                minute = 59,
            ),
        )

        // When
        val result = localDateTime.toLocalDate()

        // Then
        assertEquals(date, result)
    }

    @Test
    fun `GIVEN a date WHEN converting it to a UTC timestamp THEN points to its UTC midnight`() {
        // When
        val timestamp = date.toTimestampUTC()

        // Then
        assertEquals(1_710_460_800_000L, timestamp)
    }

    @Test
    fun `GIVEN an edited date and time WHEN computing the final timestamp THEN lands on that local time`() {
        // Given
        val eventDuration = 9.hours + 30.minutes

        // When
        val timestamp = GetFinalTimestampAfterEditionUseCase()(
            selectedLocalDate = date,
            eventDuration = eventDuration,
        )

        // Then
        assertEquals(
            LocalDateTime(
                date = date,
                time = LocalTime(
                    hour = 9,
                    minute = 30,
                ),
            ),
            LocalDateTimeProviderImpl().getLocalDateTime(timestamp),
        )
    }

    @Test
    fun `GIVEN only an edited date WHEN computing the final timestamp THEN lands on its local midnight`() {
        // When
        val timestamp = GetFinalTimestampAfterEditionUseCase()(selectedLocalDate = date)

        // Then
        assertEquals(
            LocalDateTime(
                date = date,
                time = LocalTime(
                    hour = 0,
                    minute = 0,
                ),
            ),
            LocalDateTimeProviderImpl().getLocalDateTime(timestamp),
        )
    }
}
