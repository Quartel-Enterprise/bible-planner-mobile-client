package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ListeningWordHighlightTest {
    private val style = SpanStyle(background = Color.Blue)

    @Test
    fun `GIVEN a word in the text WHEN highlighting THEN styles only that word`() {
        // Given
        val text = "In the beginning"

        // When
        val highlighted = text.withListeningWord(
            word = 3..5,
            style = style,
        )

        // Then
        assertEquals(
            listOf(AnnotatedString.Range(item = style, start = 3, end = 6)),
            highlighted.spanStyles,
        )
    }

    @Test
    fun `GIVEN a word past the end of the text WHEN highlighting THEN leaves the text plain`() {
        // Given
        val text = "In the beginning"

        // When
        val highlighted = text.withListeningWord(
            word = 14..20,
            style = style,
        )

        // Then
        assertEquals(emptyList(), highlighted.spanStyles)
    }

    @Test
    fun `GIVEN no word WHEN highlighting THEN leaves the text plain`() {
        // Given
        val text = "In the beginning"

        // When
        val highlighted = text.withListeningWord(
            word = null,
            style = style,
        )

        // Then
        assertEquals(AnnotatedString(text), highlighted)
    }

    @Test
    fun `GIVEN a reader text with leading spaces WHEN mapping a trimmed range THEN shifts it past them`() {
        // Given
        val readerText = "  In the beginning"

        // When
        val range = (3..5).toUntrimmed(readerText)

        // Then
        assertEquals("the", readerText.substring(range))
    }
}
