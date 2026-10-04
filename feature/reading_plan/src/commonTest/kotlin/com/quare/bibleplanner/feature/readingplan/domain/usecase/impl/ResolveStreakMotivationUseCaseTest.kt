package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.Streak
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

internal class ResolveStreakMotivationUseCaseTest {
    private val systemTimeZone = TimeZone.currentSystemDefault()
    private val provider = LocalDateTimeProvider { timestamp ->
        Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(systemTimeZone)
    }
    private val useCase = ResolveStreakMotivationUseCase(provider)
    private val today = LocalDate(2026, 5, 24)

    @Test
    fun `GIVEN a single read today WHEN resolving THEN returns Day1`() {
        // Given
        val days = listOf(day(readTimestamp = today.toEpochMillisLocal()))

        // When
        val streak = useCase(days, today)

        // Then
        assertEquals(Streak.Day1, streak)
    }

    @Test
    fun `GIVEN 3 consecutive days WHEN resolving THEN returns Day3`() {
        // Given
        val days = (0..2).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }

        // When
        val streak = useCase(days, today)

        // Then
        assertEquals(Streak.Day3, streak)
    }

    @Test
    fun `GIVEN 5 consecutive days WHEN resolving THEN returns Day3`() {
        // Given
        val days = (0..4).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }

        // When
        val streak = useCase(days, today)

        // Then
        assertEquals(Streak.Day3, streak)
    }

    @Test
    fun `GIVEN 7 consecutive days WHEN resolving THEN returns Day7`() {
        // Given
        val days = (0..6).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }

        // When
        val streak = useCase(days, today)

        // Then
        assertEquals(Streak.Day7, streak)
    }

    @Test
    fun `GIVEN 14 consecutive days WHEN resolving THEN returns Day14`() {
        // Given
        val days = (0..13).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }

        // When
        val streak = useCase(days, today)

        // Then
        assertEquals(Streak.Day14, streak)
    }

    @Test
    fun `GIVEN 30 consecutive days WHEN resolving THEN returns Day30`() {
        // Given
        val days = (0..29).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }

        // When
        val streak = useCase(days, today)

        // Then
        assertEquals(Streak.Day30, streak)
    }

    @Test
    fun `GIVEN 100 consecutive days WHEN resolving THEN returns Day100`() {
        // Given
        val days = (0..99).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }

        // When
        val streak = useCase(days, today)

        // Then
        assertEquals(Streak.Day100, streak)
    }

    @Test
    fun `GIVEN the last read yesterday WHEN resolving THEN returns null`() {
        // Given
        val days = listOf(
            day(readTimestamp = today.minus(1, DateTimeUnit.DAY).toEpochMillisLocal()),
        )

        // When
        val streak = useCase(days, today)

        // Then
        assertNull(streak)
    }

    @Test
    fun `GIVEN a gap between reads WHEN resolving THEN the gap breaks the streak`() {
        // Given
        val days = listOf(
            day(number = 1, readTimestamp = today.toEpochMillisLocal()),
            day(number = 2, readTimestamp = today.minus(2, DateTimeUnit.DAY).toEpochMillisLocal()),
        )

        // When
        val streak = useCase(days, today)

        // Then
        assertEquals(Streak.Day1, streak)
    }

    @Test
    fun `GIVEN no days WHEN resolving THEN returns null`() {
        // Given
        val days = emptyList<DayModel>()

        // When
        val streak = useCase(days, today)

        // Then
        assertNull(streak)
    }
}
