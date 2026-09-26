package com.quare.bibleplanner.feature.editplanstartdate.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ConvertTimestampToDatePickerInitialDateUseCaseTest {
    private val localTimeZone = TimeZone.currentSystemDefault()
    private lateinit var useCase: ConvertTimestampToDatePickerInitialDateUseCase

    @Test
    fun `GIVEN a timestamp in the afternoon WHEN converting it THEN returns the local midnight of that day`() {
        // Given
        val afternoon = LocalDateTime(
            year = 2024,
            month = 3,
            day = 15,
            hour = 16,
            minute = 30,
        ).toInstant(localTimeZone).toEpochMilliseconds()

        // When
        val initialDate = useCase(afternoon)

        // Then
        val expected = LocalDate(
            year = 2024,
            month = 3,
            day = 15,
        ).atStartOfDayIn(localTimeZone).toEpochMilliseconds()
        assertEquals(expected, initialDate)
    }

    @BeforeTest
    fun setUp() {
        useCase = ConvertTimestampToDatePickerInitialDateUseCase()
    }
}
