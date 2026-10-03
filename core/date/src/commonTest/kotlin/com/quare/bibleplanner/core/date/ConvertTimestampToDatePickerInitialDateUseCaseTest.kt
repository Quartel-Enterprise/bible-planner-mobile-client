package com.quare.bibleplanner.core.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ConvertTimestampToDatePickerInitialDateUseCaseTest {
    private val localDate = LocalDate(
        year = 2026,
        month = 5,
        day = 10,
    )
    private val eastOfUtc = TimeZone.of("Asia/Tokyo")
    private val westOfUtc = TimeZone.of("America/Sao_Paulo")
    private lateinit var timeZone: TimeZone
    private lateinit var useCase: ConvertTimestampToDatePickerInitialDateUseCase

    @Test
    fun `GIVEN a morning east of UTC WHEN converting THEN returns the UTC midnight of the local day`() {
        // Given
        prepareScenario(timeZone = eastOfUtc)
        val timestamp = timestampAt(LocalTime(hour = 8, minute = 0))

        // When
        val initialDate = useCase(timestamp)

        // Then
        assertEquals(localDate.toTimestampUTC(), initialDate)
    }

    @Test
    fun `GIVEN a late night west of UTC WHEN converting THEN returns the UTC midnight of the local day`() {
        // Given
        prepareScenario(timeZone = westOfUtc)
        val timestamp = timestampAt(LocalTime(hour = 22, minute = 0))

        // When
        val initialDate = useCase(timestamp)

        // Then
        assertEquals(localDate.toTimestampUTC(), initialDate)
    }

    @Test
    fun `GIVEN local midnight east of UTC WHEN read back as UTC THEN shows the same local day`() {
        // Given
        prepareScenario(timeZone = eastOfUtc)
        val timestamp = timestampAt(LocalTime(hour = 0, minute = 0))

        // When
        val initialDate = useCase(timestamp)

        // Then
        assertEquals(localDate, ConvertUtcDateToLocalDateUseCase()(initialDate))
    }

    private fun timestampAt(localTime: LocalTime): Long = LocalDateTime(
        date = localDate,
        time = localTime,
    ).toInstant(timeZone).toEpochMilliseconds()

    private fun prepareScenario(timeZone: TimeZone) {
        this.timeZone = timeZone
        useCase = ConvertTimestampToDatePickerInitialDateUseCase(
            localDateTimeProvider = { timestamp ->
                Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timeZone)
            },
        )
    }
}
