package com.quare.bibleplanner.core.books.util

import kotlin.test.Test
import kotlin.test.assertEquals

internal class VerseReferenceHelperTest {
    @Test
    fun `GIVEN a single verse WHEN building the label THEN renders its own number`() {
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
    fun `GIVEN consecutive verses WHEN building the label THEN collapses them into a range`() {
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
    fun `GIVEN a selection with gaps WHEN building the label THEN separates its ranges`() {
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
    fun `GIVEN repeated verse numbers WHEN building the label THEN ignores the repetitions`() {
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
