package com.quare.bibleplanner.feature.read.presentation.listening

import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_language_english
import bibleplanner.feature.read.generated.resources.listening_language_portuguese
import bibleplanner.feature.read.generated.resources.listening_language_spanish
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

    @Test
    fun `GIVEN language tags WHEN naming their languages THEN falls back to English`() {
        // Given
        val tags = listOf("pt-BR", "es-MX", "en-US", "fr-FR")

        // When
        val resources = tags.map { it.toLanguageNameResource() }

        // Then
        assertEquals(
            listOf(
                Res.string.listening_language_portuguese,
                Res.string.listening_language_spanish,
                Res.string.listening_language_english,
                Res.string.listening_language_english,
            ),
            resources,
        )
    }
}
