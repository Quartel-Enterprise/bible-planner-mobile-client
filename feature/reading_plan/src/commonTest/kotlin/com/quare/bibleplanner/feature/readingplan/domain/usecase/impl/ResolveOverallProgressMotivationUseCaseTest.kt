package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.OverallProgress
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ResolveOverallProgressMotivationUseCaseTest {
    private val useCase = ResolveOverallProgressMotivationUseCase()

    @Test
    fun `GIVEN a progress of 0 WHEN resolving THEN returns Zero`() {
        // Given
        val progress = 0f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.Zero,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 1 WHEN resolving THEN returns EarlyStart`() {
        // Given
        val progress = 1f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.EarlyStart,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 5 WHEN resolving THEN returns EarlyStart`() {
        // Given
        val progress = 5f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.EarlyStart,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 6 WHEN resolving THEN returns BuildingSolid`() {
        // Given
        val progress = 6f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.BuildingSolid,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 15 WHEN resolving THEN returns BuildingSolid`() {
        // Given
        val progress = 15f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.BuildingSolid,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 16 WHEN resolving THEN returns ApproachingThird`() {
        // Given
        val progress = 16f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.ApproachingThird,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 30 WHEN resolving THEN returns ApproachingThird`() {
        // Given
        val progress = 30f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.ApproachingThird,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 31 WHEN resolving THEN returns PastThirty`() {
        // Given
        val progress = 31f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.PastThirty,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 49 WHEN resolving THEN returns PastThirty`() {
        // Given
        val progress = 49f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.PastThirty,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 50 WHEN resolving THEN returns Halfway`() {
        // Given
        val progress = 50f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.Halfway,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 51 WHEN resolving THEN returns MoreThanHalf`() {
        // Given
        val progress = 51f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.MoreThanHalf,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 74 WHEN resolving THEN returns MoreThanHalf`() {
        // Given
        val progress = 74f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.MoreThanHalf,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 75 WHEN resolving THEN returns ThreeQuarters`() {
        // Given
        val progress = 75f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.ThreeQuarters,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 76 WHEN resolving THEN returns FinalStretch`() {
        // Given
        val progress = 76f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.FinalStretch,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 90 WHEN resolving THEN returns FinalStretch`() {
        // Given
        val progress = 90f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.FinalStretch,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 91 WHEN resolving THEN returns AlmostThere`() {
        // Given
        val progress = 91f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.AlmostThere,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 99 WHEN resolving THEN returns AlmostThere`() {
        // Given
        val progress = 99f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.AlmostThere,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress of 100 WHEN resolving THEN returns Completed`() {
        // Given
        val progress = 100f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.Completed,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a negative progress WHEN resolving THEN clamps to Zero`() {
        // Given
        val progress = -5f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.Zero,
            actual = message,
        )
    }

    @Test
    fun `GIVEN a progress over 100 WHEN resolving THEN clamps to Completed`() {
        // Given
        val progress = 150f

        // When
        val message = useCase(progress)

        // Then
        assertEquals(
            expected = OverallProgress.Completed,
            actual = message,
        )
    }
}
