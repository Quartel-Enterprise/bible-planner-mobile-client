package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.plan.DayModel
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
    fun `GIVEN today fully read WHEN resolving THEN returns Completed`() {
        // Given
        val days = listOf(
            day(isToday = true, isRead = true, readVerses = 10, totalVerses = 10),
        )

        // When
        val situation = useCase(days, today)

        // Then
        assertEquals(DaySituation.Completed, situation)
    }

    @Test
    fun `GIVEN today partially read WHEN resolving THEN returns Started`() {
        // Given
        val days = listOf(
            day(isToday = true, isRead = false, readVerses = 3, totalVerses = 10),
        )

        // When
        val situation = useCase(days, today)

        // Then
        assertEquals(DaySituation.Started, situation)
    }

    @Test
    fun `GIVEN today not started and no overdue day WHEN resolving THEN returns NotStarted`() {
        // Given
        val days = listOf(
            day(isToday = true, isRead = false, readVerses = 0, totalVerses = 10),
        )

        // When
        val situation = useCase(days, today)

        // Then
        assertEquals(DaySituation.NotStarted, situation)
    }

    @Test
    fun `GIVEN one overdue day WHEN resolving THEN returns OneOverdue`() {
        // Given
        val days = listOf(
            day(
                number = 1,
                isRead = false,
                isToday = false,
                totalVerses = 10,
                plannedReadDate = today.minus(1, DateTimeUnit.DAY),
            ),
        )

        // When
        val situation = useCase(days, today)

        // Then
        assertEquals(DaySituation.OneOverdue, situation)
    }

    @Test
    fun `GIVEN multiple overdue days WHEN resolving THEN returns MultipleOverdue`() {
        // Given
        val days = listOf(
            day(number = 1, isRead = false, plannedReadDate = today.minus(2, DateTimeUnit.DAY)),
            day(number = 2, isRead = false, plannedReadDate = today.minus(1, DateTimeUnit.DAY)),
        )

        // When
        val situation = useCase(days, today)

        // Then
        assertEquals(DaySituation.MultipleOverdue, situation)
    }

    @Test
    fun `GIVEN today completed and an overdue day WHEN resolving THEN returns Completed`() {
        // Given
        val days = listOf(
            day(number = 1, isRead = false, plannedReadDate = today.minus(1, DateTimeUnit.DAY)),
            day(number = 2, isToday = true, isRead = true, readVerses = 10, totalVerses = 10),
        )

        // When
        val situation = useCase(days, today)

        // Then
        assertEquals(DaySituation.Completed, situation)
    }

    @Test
    fun `GIVEN today started and an overdue day WHEN resolving THEN returns Started`() {
        // Given
        val days = listOf(
            day(number = 1, isRead = false, plannedReadDate = today.minus(1, DateTimeUnit.DAY)),
            day(number = 2, isToday = true, isRead = false, readVerses = 4, totalVerses = 10),
        )

        // When
        val situation = useCase(days, today)

        // Then
        assertEquals(DaySituation.Started, situation)
    }

    @Test
    fun `GIVEN an overdue day and today not started WHEN resolving THEN returns OneOverdue`() {
        // Given
        val days = listOf(
            day(number = 1, isRead = false, plannedReadDate = today.minus(1, DateTimeUnit.DAY)),
            day(number = 2, isToday = true, isRead = false, readVerses = 0, totalVerses = 10),
        )

        // When
        val situation = useCase(days, today)

        // Then
        assertEquals(DaySituation.OneOverdue, situation)
    }

    @Test
    fun `GIVEN an empty rest day today WHEN resolving THEN returns null`() {
        // Given
        val days = listOf(
            day(isToday = true, isRead = false, readVerses = 0, totalVerses = 0),
        )

        // When
        val situation = useCase(days, today)

        // Then
        assertNull(situation)
    }

    @Test
    fun `GIVEN no today and no overdue day WHEN resolving THEN returns null`() {
        // Given
        val days = emptyList<DayModel>()

        // When
        val situation = useCase(days, today)

        // Then
        assertNull(situation)
    }
}
