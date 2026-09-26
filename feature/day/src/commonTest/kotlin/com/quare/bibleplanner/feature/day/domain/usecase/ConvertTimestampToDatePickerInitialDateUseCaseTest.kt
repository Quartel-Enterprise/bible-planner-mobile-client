package com.quare.bibleplanner.feature.day.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ConvertTimestampToDatePickerInitialDateUseCaseTest {
    private val timeZone = TimeZone.currentSystemDefault()
    private lateinit var useCase: ConvertTimestampToDatePickerInitialDateUseCase

    @BeforeTest
    fun setUp() {
        useCase = ConvertTimestampToDatePickerInitialDateUseCase()
    }

    @Test
    fun `GIVEN a timestamp in the middle of a local day WHEN converting THEN returns the local midnight of that day`() {
        // Given
        val timestamp = LocalDateTime(LocalDate(2026, 5, 10), LocalTime(15, 45))
            .toInstant(timeZone)
            .toEpochMilliseconds()

        // When
        val initialDate = useCase(timestamp)

        // Then
        assertEquals(LocalDate(2026, 5, 10).atStartOfDayIn(timeZone).toEpochMilliseconds(), initialDate)
    }

    @Test
    fun `GIVEN a local midnight timestamp WHEN converting THEN returns it unchanged`() {
        // Given
        val midnight = LocalDate(2026, 12, 31).atStartOfDayIn(timeZone).toEpochMilliseconds()

        // When
        val initialDate = useCase(midnight)

        // Then
        assertEquals(midnight, initialDate)
    }
}
