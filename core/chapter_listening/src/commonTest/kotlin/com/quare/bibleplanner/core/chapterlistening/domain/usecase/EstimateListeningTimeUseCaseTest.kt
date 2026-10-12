package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

internal class EstimateListeningTimeUseCaseTest {
    private val useCase = EstimateListeningTimeUseCase()

    @Test
    fun `GIVEN two verses of five words WHEN estimating THEN reads them at the voice pace`() {
        // Given
        val verseTexts = listOf("one two three four five", "six seven eight nine ten")

        // When
        val time = useCase(
            verseTexts = verseTexts,
            speed = 1f,
        )

        // Then
        assertEquals(4.seconds, time)
    }

    @Test
    fun `GIVEN double speed WHEN estimating THEN halves the time`() {
        // Given
        val speed = 2f

        // When
        val time = useCase(
            verseTexts = listOf("one two three four five", "six seven eight nine ten"),
            speed = speed,
        )

        // Then
        assertEquals(2.seconds, time)
    }

    @Test
    fun `GIVEN a verse with extra spaces WHEN estimating THEN ignores the blanks`() {
        // Given
        val verseTexts = listOf("  six  seven eight nine ten ")

        // When
        val time = useCase(
            verseTexts = verseTexts,
            speed = 1f,
        )

        // Then
        assertEquals(2.seconds, time)
    }
}
