package com.quare.bibleplanner.feature.day.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

internal class ConvertUtcDateToLocalDateUseCaseTest {
    private lateinit var useCase: ConvertUtcDateToLocalDateUseCase

    @BeforeTest
    fun setUp() {
        useCase = ConvertUtcDateToLocalDateUseCase()
    }

    @Test
    fun `GIVEN a UTC midnight timestamp WHEN converting THEN returns that calendar date`() {
        // Given
        val utcMillis = LocalDate(2026, 1, 31).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

        // When
        val date = useCase(utcMillis)

        // Then
        assertEquals(LocalDate(2026, 1, 31), date)
    }

    @Test
    fun `GIVEN a late UTC timestamp WHEN converting THEN keeps the UTC calendar date`() {
        // Given
        val utcMillis = LocalDate(2026, 1, 31).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() +
            23.hours.inWholeMilliseconds

        // When
        val date = useCase(utcMillis)

        // Then
        assertEquals(LocalDate(2026, 1, 31), date)
    }
}
