package com.quare.bibleplanner.feature.read.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class VerseNoteMarkPositionTest {
    @Test
    fun `GIVEN every note mark position WHEN filtering by icon THEN only the start of a note carries the icon`() {
        // Given
        val positions = VerseNoteMarkPosition.entries

        // When
        val withIcon = positions.filter { it.hasIcon }

        // Then
        assertEquals(
            expected = listOf(VerseNoteMarkPosition.SINGLE, VerseNoteMarkPosition.FIRST),
            actual = withIcon,
        )
    }

    @Test
    fun `GIVEN every note mark position WHEN filtering the linked ones THEN the bar joins verses of a note`() {
        // Given
        val positions = VerseNoteMarkPosition.entries

        // When
        val linkedAbove = positions.filter { it.isLinkedAbove }
        val linkedBelow = positions.filter { it.isLinkedBelow }

        // Then
        assertEquals(
            expected = listOf(VerseNoteMarkPosition.MIDDLE, VerseNoteMarkPosition.LAST),
            actual = linkedAbove,
        )
        assertEquals(
            expected = listOf(VerseNoteMarkPosition.FIRST, VerseNoteMarkPosition.MIDDLE),
            actual = linkedBelow,
        )
    }
}
