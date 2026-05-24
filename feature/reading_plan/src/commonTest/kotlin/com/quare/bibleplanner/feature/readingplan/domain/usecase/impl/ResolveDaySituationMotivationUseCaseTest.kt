package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.DaySituation
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ResolveDaySituationMotivationUseCaseTest {
    private val useCase = ResolveDaySituationMotivationUseCase()
    private val today = LocalDate(2026, 5, 24)

    @Test
    fun `today fully read returns Completed`() {
        val days = listOf(
            day(isToday = true, isRead = true, readVerses = 10, totalVerses = 10),
        )
        assertEquals(DaySituation.Completed, useCase(days, today))
    }

    @Test
    fun `today partially read returns Started`() {
        val days = listOf(
            day(isToday = true, isRead = false, readVerses = 3, totalVerses = 10),
        )
        assertEquals(DaySituation.Started, useCase(days, today))
    }

    @Test
    fun `today not started with no overdue returns NotStarted`() {
        val days = listOf(
            day(isToday = true, isRead = false, readVerses = 0, totalVerses = 10),
        )
        assertEquals(DaySituation.NotStarted, useCase(days, today))
    }

    @Test
    fun `one overdue returns OneOverdue`() {
        val days = listOf(
            day(
                number = 1,
                isRead = false,
                isToday = false,
                totalVerses = 10,
                plannedReadDate = today.minus(1, DateTimeUnit.DAY),
            ),
        )
        assertEquals(DaySituation.OneOverdue, useCase(days, today))
    }

    @Test
    fun `multiple overdue returns MultipleOverdue`() {
        val days = listOf(
            day(number = 1, isRead = false, plannedReadDate = today.minus(2, DateTimeUnit.DAY)),
            day(number = 2, isRead = false, plannedReadDate = today.minus(1, DateTimeUnit.DAY)),
        )
        assertEquals(DaySituation.MultipleOverdue, useCase(days, today))
    }

    @Test
    fun `today completed beats overdue`() {
        val days = listOf(
            day(number = 1, isRead = false, plannedReadDate = today.minus(1, DateTimeUnit.DAY)),
            day(number = 2, isToday = true, isRead = true, readVerses = 10, totalVerses = 10),
        )
        assertEquals(DaySituation.Completed, useCase(days, today))
    }

    @Test
    fun `today started beats overdue`() {
        val days = listOf(
            day(number = 1, isRead = false, plannedReadDate = today.minus(1, DateTimeUnit.DAY)),
            day(number = 2, isToday = true, isRead = false, readVerses = 4, totalVerses = 10),
        )
        assertEquals(DaySituation.Started, useCase(days, today))
    }

    @Test
    fun `overdue beats not-started`() {
        val days = listOf(
            day(number = 1, isRead = false, plannedReadDate = today.minus(1, DateTimeUnit.DAY)),
            day(number = 2, isToday = true, isRead = false, readVerses = 0, totalVerses = 10),
        )
        assertEquals(DaySituation.OneOverdue, useCase(days, today))
    }

    @Test
    fun `empty rest day today returns null`() {
        val days = listOf(
            day(isToday = true, isRead = false, readVerses = 0, totalVerses = 0),
        )
        assertNull(useCase(days, today))
    }

    @Test
    fun `no today and no overdue returns null`() {
        assertNull(useCase(emptyList(), today))
    }
}
