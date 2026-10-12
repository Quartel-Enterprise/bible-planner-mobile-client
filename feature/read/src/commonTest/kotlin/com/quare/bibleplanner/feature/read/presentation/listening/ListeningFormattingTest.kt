package com.quare.bibleplanner.feature.read.presentation.listening

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

internal class ListeningFormattingTest {
    @Test
    fun `GIVEN durations WHEN formatting them as a clock THEN pads the seconds`() {
        // Given
        val durations = listOf(75.seconds, 5.seconds, (-3).seconds)

        // When
        val texts = durations.map { it.toClockText() }

        // Then
        assertEquals(listOf("1:15", "0:05", "0:00"), texts)
    }

    @Test
    fun `GIVEN a short chapter WHEN estimating its minutes THEN says at least one`() {
        // Given
        val verseTexts = listOf("In the beginning")

        // When
        val minutes = getListeningMinutes(
            verseTexts = verseTexts,
            speed = 1f,
        )

        // Then
        assertEquals(1, minutes)
    }

    @Test
    fun `GIVEN a long chapter WHEN estimating its minutes THEN rounds the words at the voice pace`() {
        // Given
        val verseTexts = List(30) { "word ".repeat(25) }

        // When
        val minutes = getListeningMinutes(
            verseTexts = verseTexts,
            speed = 1f,
        )

        // Then
        assertEquals(5, minutes)
    }
}
