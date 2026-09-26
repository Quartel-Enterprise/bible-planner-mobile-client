package com.quare.bibleplanner.core.date

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

internal class DateRepresentationTest {
    private lateinit var today: LocalDate

    @BeforeTest
    fun setUp() {
        today = Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }

    @Test
    fun `GIVEN the days around today WHEN representing them THEN names them relative to today`() {
        // When
        val representations = listOf(
            daysAgo(-1),
            today,
            daysAgo(1),
        ).map(LocalDate::toDateRepresentation)

        // Then
        assertEquals(
            listOf(
                DateRepresentation.Tomorrow,
                DateRepresentation.Today,
                DateRepresentation.Yesterday,
            ),
            representations,
        )
    }

    @Test
    fun `GIVEN days within the last two weeks WHEN representing them THEN counts days or says last week`() {
        // When
        val representations = listOf(2, 6, 7, 13).map { days -> daysAgo(days).toDateRepresentation() }

        // Then
        assertEquals(
            listOf(
                DateRepresentation.DaysAgo(2),
                DateRepresentation.DaysAgo(6),
                DateRepresentation.LastWeek,
                DateRepresentation.LastWeek,
            ),
            representations,
        )
    }

    @Test
    fun `GIVEN two to four weeks ago WHEN representing it THEN counts whole weeks`() {
        // When
        val representations = listOf(14, 20, 27).map { days -> daysAgo(days).toDateRepresentation() }

        // Then
        assertEquals(
            listOf(
                DateRepresentation.WeeksAgo(2),
                DateRepresentation.WeeksAgo(2),
                DateRepresentation.WeeksAgo(3),
            ),
            representations,
        )
    }

    @Test
    fun `GIVEN months ago WHEN representing it THEN says last month or counts the months`() {
        // When
        val representations = listOf(1, 3, 14).map { months ->
            today
                .minus(
                    value = months,
                    unit = DateTimeUnit.MONTH,
                ).toDateRepresentation()
        }

        // Then
        assertEquals(
            listOf(
                DateRepresentation.LastMonth,
                DateRepresentation.MonthsAgo(3),
                DateRepresentation.MonthsAgo(14),
            ),
            representations,
        )
    }

    @Test
    fun `GIVEN a date after tomorrow WHEN representing it THEN shows the date itself`() {
        // Given
        val date = daysAgo(-5)

        // When
        val representation = date.toDateRepresentation()

        // Then
        assertEquals(DateRepresentation.Custom(date), representation)
    }

    private fun daysAgo(days: Int): LocalDate = today.minus(
        value = days,
        unit = DateTimeUnit.DAY,
    )
}
