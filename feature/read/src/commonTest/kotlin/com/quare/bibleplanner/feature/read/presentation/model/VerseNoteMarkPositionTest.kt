package com.quare.bibleplanner.feature.read.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class VerseNoteMarkPositionTest {
    @Test
    fun `only the start of a note carries the icon`() {
        // When
        val withIcon = VerseNoteMarkPosition.entries.filter { it.hasIcon }

        // Then
        assertEquals(
            expected = listOf(VerseNoteMarkPosition.SINGLE, VerseNoteMarkPosition.FIRST),
            actual = withIcon,
        )
    }

    @Test
    fun `the bar joins a verse to the verses of its note above and below`() {
        // When
        val linkedAbove = VerseNoteMarkPosition.entries.filter { it.isLinkedAbove }
        val linkedBelow = VerseNoteMarkPosition.entries.filter { it.isLinkedBelow }

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
