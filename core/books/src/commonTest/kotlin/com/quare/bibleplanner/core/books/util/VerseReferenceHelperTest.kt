package com.quare.bibleplanner.core.books.util

import kotlin.test.Test
import kotlin.test.assertEquals

internal class VerseReferenceHelperTest {
    @Test
    fun `renders a single verse as its own number`() {
        // Given
        val verseNumbers = listOf(3)

        // When
        val label = verseNumbers.toVerseNumbersLabel()

        // Then
        assertEquals(
            expected = "3",
            actual = label,
        )
    }

    @Test
    fun `collapses consecutive verses into a range`() {
        // Given
        val verseNumbers = listOf(1, 2, 3)

        // When
        val label = verseNumbers.toVerseNumbersLabel()

        // Then
        assertEquals(
            expected = "1-3",
            actual = label,
        )
    }

    @Test
    fun `separates the ranges of a selection with gaps`() {
        // Given
        val verseNumbers = listOf(7, 1, 2, 3, 9, 10)

        // When
        val label = verseNumbers.toVerseNumbersLabel()

        // Then
        assertEquals(
            expected = "1-3, 7, 9-10",
            actual = label,
        )
    }

    @Test
    fun `ignores repeated verse numbers`() {
        // Given
        val verseNumbers = listOf(2, 2, 3)

        // When
        val label = verseNumbers.toVerseNumbersLabel()

        // Then
        assertEquals(
            expected = "2-3",
            actual = label,
        )
    }
}
