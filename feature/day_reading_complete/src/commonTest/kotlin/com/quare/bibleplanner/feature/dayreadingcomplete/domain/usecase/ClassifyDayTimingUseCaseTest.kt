package com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase

import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

private val today = LocalDate(2026, 8, 21)

internal class ClassifyDayTimingUseCaseTest {
    private val classifyDayTiming = ClassifyDayTimingUseCase(
        currentTimestampProvider = { 0L },
        localDateTimeProvider = { LocalDateTime(today, LocalTime(12, 0)) },
    )

    @Test
    fun `classifies a day planned for today as on time`() {
        assertEquals(
            expected = DayTimingState.ON_TIME,
            actual = classifyDayTiming(today),
        )
    }

    @Test
    fun `classifies a day planned before today as overdue`() {
        assertEquals(
            expected = DayTimingState.OVERDUE,
            actual = classifyDayTiming(LocalDate(2026, 8, 17)),
        )
    }

    @Test
    fun `classifies a day planned after today as early`() {
        assertEquals(
            expected = DayTimingState.EARLY,
            actual = classifyDayTiming(LocalDate(2026, 8, 28)),
        )
    }

    @Test
    fun `falls back to on time when the day has no planned date`() {
        assertEquals(
            expected = DayTimingState.ON_TIME,
            actual = classifyDayTiming(null),
        )
    }
}
