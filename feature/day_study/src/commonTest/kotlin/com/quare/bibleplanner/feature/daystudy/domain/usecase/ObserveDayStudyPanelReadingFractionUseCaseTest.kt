package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPanelRatio
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveDayStudyPanelReadingFractionUseCaseTest {
    @Test
    fun `GIVEN no stored fraction WHEN observing THEN emits the default`() = runTest {
        // Given
        val useCase = useCaseWith(storedFraction = null)

        // When
        val emitted = useCase().first()

        // Then
        assertEquals(DayStudyPanelRatio.DEFAULT, emitted)
    }

    @Test
    fun `GIVEN a stored fraction below the minimum WHEN observing THEN clamps to the minimum`() = runTest {
        // Given
        val useCase = useCaseWith(storedFraction = 0.1f)

        // When
        val emitted = useCase().first()

        // Then
        assertEquals(DayStudyPanelRatio.MIN, emitted)
    }

    @Test
    fun `GIVEN a stored fraction above the maximum WHEN observing THEN clamps to the maximum`() = runTest {
        // Given
        val useCase = useCaseWith(storedFraction = 0.9f)

        // When
        val emitted = useCase().first()

        // Then
        assertEquals(DayStudyPanelRatio.MAX, emitted)
    }

    @Test
    fun `GIVEN a stored fraction within range WHEN observing THEN emits it unchanged`() = runTest {
        // Given
        val useCase = useCaseWith(storedFraction = 0.5f)

        // When
        val emitted = useCase().first()

        // Then
        assertEquals(0.5f, emitted)
    }

    private fun useCaseWith(storedFraction: Float?) = ObserveDayStudyPanelReadingFractionUseCase(
        repository = FakeDayStudyPanelRatioRepository(initialFraction = storedFraction),
    )
}
