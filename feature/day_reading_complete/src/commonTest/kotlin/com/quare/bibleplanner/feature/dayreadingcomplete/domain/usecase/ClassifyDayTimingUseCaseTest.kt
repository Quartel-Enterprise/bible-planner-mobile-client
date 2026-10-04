package com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase

import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ClassifyDayTimingUseCaseTest {
    private val today = LocalDate(2026, 8, 21)
    private val classifyDayTiming = ClassifyDayTimingUseCase(
        currentTimestampProvider = { 0L },
        localDateTimeProvider = { LocalDateTime(today, LocalTime(12, 0)) },
    )

    @Test
    fun `GIVEN a day planned for today WHEN classifying THEN returns on time`() {
        // Given
        val plannedDate = today

        // When
        val timing = classifyDayTiming(plannedDate)

        // Then
        assertEquals(
            expected = DayTimingState.ON_TIME,
            actual = timing,
        )
    }

    @Test
    fun `GIVEN a day planned before today WHEN classifying THEN returns overdue`() {
        // Given
        val plannedDate = LocalDate(2026, 8, 17)

        // When
        val timing = classifyDayTiming(plannedDate)

        // Then
        assertEquals(
            expected = DayTimingState.OVERDUE,
            actual = timing,
        )
    }

    @Test
    fun `GIVEN a day planned after today WHEN classifying THEN returns early`() {
        // Given
        val plannedDate = LocalDate(2026, 8, 28)

        // When
        val timing = classifyDayTiming(plannedDate)

        // Then
        assertEquals(
            expected = DayTimingState.EARLY,
            actual = timing,
        )
    }

    @Test
    fun `GIVEN a day with no planned date WHEN classifying THEN falls back to on time`() {
        // Given
        val plannedDate: LocalDate? = null

        // When
        val timing = classifyDayTiming(plannedDate)

        // Then
        assertEquals(
            expected = DayTimingState.ON_TIME,
            actual = timing,
        )
    }
}
