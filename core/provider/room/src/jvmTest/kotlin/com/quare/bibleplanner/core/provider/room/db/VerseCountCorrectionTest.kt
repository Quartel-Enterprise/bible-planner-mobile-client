package com.quare.bibleplanner.core.provider.room.db

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VerseCountCorrectionTest {
    @Test
    fun `GIVEN the bundled corrections WHEN reading them THEN each chapter is corrected once to a positive count`() {
        // Given
        val corrections = VERSE_COUNT_CORRECTIONS

        // When
        val correctedChapterCount = corrections.distinctBy { it.bookId to it.chapter }.size

        // Then
        assertEquals(
            expected = corrections.size,
            actual = correctedChapterCount,
        )
        assertTrue(corrections.all { it.chapter > 0 && it.verses > 0 })
    }
}
