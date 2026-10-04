package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class EstimateListeningTimeUseCaseTest {
    private val useCase = EstimateListeningTimeUseCase()
    private val wordCounts = listOf(5, 5)

    @Test
    fun `GIVEN two verses of five words WHEN halfway through the second THEN estimates the time at the voice pace`() {
        // Given
        val verseIndex = 1

        // When
        val time = useCase(
            wordCounts = wordCounts,
            verseIndex = verseIndex,
            verseProgress = 0.5f,
            speed = 1f,
        )

        // Then
        assertEquals(3.seconds, time.elapsed)
        assertEquals(4.seconds, time.total)
        assertEquals(1.seconds, time.remaining)
    }

    @Test
    fun `GIVEN double speed WHEN estimating THEN halves the time`() {
        // Given
        val speed = 2f

        // When
        val time = useCase(
            wordCounts = wordCounts,
            verseIndex = 0,
            verseProgress = 0f,
            speed = speed,
        )

        // Then
        assertEquals(2.seconds, time.total)
        assertEquals(Duration.ZERO, time.elapsed)
    }

    @Test
    fun `GIVEN a verse past its end WHEN estimating THEN never reports negative time left`() {
        // Given
        val verseProgress = 3f

        // When
        val time = useCase(
            wordCounts = wordCounts,
            verseIndex = 5,
            verseProgress = verseProgress,
            speed = 1f,
        )

        // Then
        assertEquals(Duration.ZERO, time.remaining)
    }

    @Test
    fun `GIVEN a verse with extra spaces WHEN counting its words THEN ignores the blanks`() {
        // Given
        val text = "  six  seven eight nine ten "

        // When
        val wordCount = useCase.countWords(text)

        // Then
        assertEquals(5, wordCount)
    }

    @Test
    fun `GIVEN a verse of ten words WHEN getting its duration THEN reads it at the voice pace`() {
        // Given
        val wordCount = 10

        // When
        val duration = useCase.getDuration(
            wordCount = wordCount,
            speed = 1f,
        )

        // Then
        assertEquals(4.seconds, duration)
    }
}
