package com.quare.bibleplanner.feature.day.presentation.mapper

import com.quare.bibleplanner.feature.day.domain.mapper.LocalDateTimeToDateMapper
import com.quare.bibleplanner.ui.component.date.DatePresentationModel
import com.quare.bibleplanner.ui.utils.MonthPresentationMapper
import com.quare.bibleplanner.ui.utils.toStringResource
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReadDateFormatterTest {
    private lateinit var formatter: ReadDateFormatter

    @Test
    fun `GIVEN a morning timestamp WHEN formatting THEN pads the hour and minute`() {
        // Given
        prepareScenario(
            LocalDateTime(
                year = 2026,
                month = Month.MARCH,
                day = 7,
                hour = 8,
                minute = 5,
            ),
        )

        // When
        val formatted = formatter.format(timestamp = 0L)

        // Then
        assertEquals(
            DatePresentationModel(
                day = 7,
                month = Month.MARCH.toStringResource(),
                year = 2026,
                minute = "05",
                hour = "08",
            ),
            formatted,
        )
    }

    @Test
    fun `GIVEN an evening timestamp WHEN formatting THEN keeps two digit values`() {
        // Given
        prepareScenario(
            LocalDateTime(
                year = 2025,
                month = Month.DECEMBER,
                day = 31,
                hour = 23,
                minute = 59,
            ),
        )

        // When
        val formatted = formatter.format(timestamp = 0L)

        // Then
        assertEquals("23", formatted.hour)
        assertEquals("59", formatted.minute)
        assertEquals(Month.DECEMBER.toStringResource(), formatted.month)
    }

    private fun prepareScenario(localDateTime: LocalDateTime) {
        formatter = ReadDateFormatter(
            localDateTimeToDateMapper = LocalDateTimeToDateMapper(),
            monthPresentationMapper = MonthPresentationMapper(),
            localDateTimeProvider = { localDateTime },
        )
    }
}
