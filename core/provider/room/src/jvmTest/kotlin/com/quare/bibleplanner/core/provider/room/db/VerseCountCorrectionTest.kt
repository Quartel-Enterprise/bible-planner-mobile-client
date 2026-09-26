package com.quare.bibleplanner.core.provider.room.db

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VerseCountCorrectionTest {
    @Test
    fun `GIVEN the bundled corrections WHEN reading them THEN each chapter is corrected once to a positive count`() {
        // When
        val corrections = VERSE_COUNT_CORRECTIONS

        // Then
        assertEquals(
            expected = corrections.size,
            actual = corrections.distinctBy { it.bookId to it.chapter }.size,
        )
        assertTrue(corrections.all { it.chapter > 0 && it.verses > 0 })
    }
}
