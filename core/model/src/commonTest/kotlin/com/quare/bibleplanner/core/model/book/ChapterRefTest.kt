package com.quare.bibleplanner.core.model.book

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class ChapterRefTest {
    @Test
    fun `GIVEN the same coordinates in the same version WHEN comparing THEN they are the same chapter`() {
        // Given
        val first = ChapterRef(
            bibleVersionId = "WEB",
            bookId = BookId.JHN,
            chapterNumber = 3,
        )

        // When
        val second = first.copy()

        // Then
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `GIVEN the same coordinates in another version WHEN comparing THEN they are different chapters`() {
        // Given
        val web = ChapterRef(
            bibleVersionId = "WEB",
            bookId = BookId.JHN,
            chapterNumber = 3,
        )

        // When
        val kjv = web.copy(bibleVersionId = "KJV")

        // Then
        assertNotEquals(web, kjv)
    }
}
