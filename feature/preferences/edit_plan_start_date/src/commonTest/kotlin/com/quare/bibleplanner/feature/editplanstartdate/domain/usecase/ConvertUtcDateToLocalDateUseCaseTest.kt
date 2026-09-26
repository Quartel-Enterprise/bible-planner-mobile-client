package com.quare.bibleplanner.feature.editplanstartdate.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ConvertUtcDateToLocalDateUseCaseTest {
    private lateinit var useCase: ConvertUtcDateToLocalDateUseCase

    @Test
    fun `GIVEN a date picker value at utc midnight WHEN converting it THEN returns that calendar date`() {
        // Given
        val utcMidnight = LocalDateTime(
            year = 2024,
            month = 3,
            day = 15,
            hour = 0,
            minute = 0,
        ).toInstant(TimeZone.UTC).toEpochMilliseconds()

        // When
        val date = useCase(utcMidnight)

        // Then
        assertEquals(
            LocalDate(
                year = 2024,
                month = 3,
                day = 15,
            ),
            date,
        )
    }

    @Test
    fun `GIVEN a utc time late in the day WHEN converting it THEN keeps the utc calendar date`() {
        // Given
        val utcLateEvening = LocalDateTime(
            year = 2024,
            month = 3,
            day = 15,
            hour = 23,
            minute = 59,
        ).toInstant(TimeZone.UTC).toEpochMilliseconds()

        // When
        val date = useCase(utcLateEvening)

        // Then
        assertEquals(
            LocalDate(
                year = 2024,
                month = 3,
                day = 15,
            ),
            date,
        )
    }

    @BeforeTest
    fun setUp() {
        useCase = ConvertUtcDateToLocalDateUseCase()
    }
}
