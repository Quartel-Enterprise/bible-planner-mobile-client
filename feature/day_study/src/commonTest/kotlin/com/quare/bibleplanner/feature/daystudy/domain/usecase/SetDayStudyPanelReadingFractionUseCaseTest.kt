package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPanelRatio
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SetDayStudyPanelReadingFractionUseCaseTest {
    @Test
    fun `GIVEN a fraction within range WHEN setting THEN persists it unchanged`() = runTest {
        // Given
        val repository = FakeDayStudyPanelRatioRepository(initialFraction = null)
        val useCase = SetDayStudyPanelReadingFractionUseCase(repository = repository)

        // When
        useCase(0.5f)

        // Then
        assertEquals(0.5f, repository.lastSetFraction)
    }

    @Test
    fun `GIVEN a fraction below the minimum WHEN setting THEN persists the clamped minimum`() = runTest {
        // Given
        val repository = FakeDayStudyPanelRatioRepository(initialFraction = null)
        val useCase = SetDayStudyPanelReadingFractionUseCase(repository = repository)

        // When
        useCase(0.05f)

        // Then
        assertEquals(DayStudyPanelRatio.MIN, repository.lastSetFraction)
    }

    @Test
    fun `GIVEN a fraction above the maximum WHEN setting THEN persists the clamped maximum`() = runTest {
        // Given
        val repository = FakeDayStudyPanelRatioRepository(initialFraction = null)
        val useCase = SetDayStudyPanelReadingFractionUseCase(repository = repository)

        // When
        useCase(0.95f)

        // Then
        assertEquals(DayStudyPanelRatio.MAX, repository.lastSetFraction)
    }
}
