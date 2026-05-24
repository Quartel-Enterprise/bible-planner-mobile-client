package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
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
    fun `single read today returns Day1`() {
        val days = listOf(day(readTimestamp = today.toEpochMillisLocal()))
        assertEquals(Streak.Day1, useCase(days, today))
    }

    @Test
    fun `3 consecutive days returns Day3`() {
        val days = (0..2).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }
        assertEquals(Streak.Day3, useCase(days, today))
    }

    @Test
    fun `5 consecutive days returns Day3`() {
        val days = (0..4).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }
        assertEquals(Streak.Day3, useCase(days, today))
    }

    @Test
    fun `7 consecutive days returns Day7`() {
        val days = (0..6).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }
        assertEquals(Streak.Day7, useCase(days, today))
    }

    @Test
    fun `14 consecutive days returns Day14`() {
        val days = (0..13).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }
        assertEquals(Streak.Day14, useCase(days, today))
    }

    @Test
    fun `30 consecutive days returns Day30`() {
        val days = (0..29).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }
        assertEquals(Streak.Day30, useCase(days, today))
    }

    @Test
    fun `100 consecutive days returns Day100`() {
        val days = (0..99).map { offset ->
            day(
                number = offset + 1,
                readTimestamp = today.minus(offset, DateTimeUnit.DAY).toEpochMillisLocal(),
            )
        }
        assertEquals(Streak.Day100, useCase(days, today))
    }

    @Test
    fun `last read yesterday returns null`() {
        val days = listOf(
            day(readTimestamp = today.minus(1, DateTimeUnit.DAY).toEpochMillisLocal()),
        )
        assertNull(useCase(days, today))
    }

    @Test
    fun `gap breaks streak`() {
        val days = listOf(
            day(number = 1, readTimestamp = today.toEpochMillisLocal()),
            day(number = 2, readTimestamp = today.minus(2, DateTimeUnit.DAY).toEpochMillisLocal()),
        )
        assertEquals(Streak.Day1, useCase(days, today))
    }

    @Test
    fun `empty days returns null`() {
        assertNull(useCase(emptyList(), today))
    }
}
