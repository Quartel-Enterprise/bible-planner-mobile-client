package com.quare.bibleplanner.core.books.domain

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.VerseModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ChapterReadProgressTest {
    @Test
    fun `GIVEN a chapter flagged read WHEN counting read verses THEN counts every verse`() {
        // Given
        val chapter = chapter(
            isRead = true,
            readVerses = setOf(1),
        )

        // When
        val count = chapter.readVersesCount

        // Then
        assertEquals(4, count)
    }

    @Test
    fun `GIVEN a partially read chapter WHEN counting read verses THEN counts only the read ones`() {
        // Given
        val chapter = chapter(
            isRead = false,
            readVerses = setOf(1, 3),
        )

        // When
        val count = chapter.readVersesCount

        // Then
        assertEquals(2, count)
    }

    @Test
    fun `GIVEN a chapter flagged read WHEN checking any verse THEN it counts as read`() {
        // Given
        val chapter = chapter(
            isRead = true,
            readVerses = emptySet(),
        )

        // When
        val isRead = chapter.isVerseRead(2)

        // Then
        assertTrue(isRead)
    }

    @Test
    fun `GIVEN a bounded range WHEN every verse in it is read THEN the range is read`() {
        // Given
        val chapter = chapter(
            isRead = false,
            readVerses = setOf(2, 3),
        )

        // When
        val isRead = chapter.isRangeRead(
            startVerse = 2,
            endVerse = 3,
        )

        // Then
        assertTrue(isRead)
    }

    @Test
    fun `GIVEN an open range WHEN a later verse is unread THEN the range is not read`() {
        // Given
        val chapter = chapter(
            isRead = false,
            readVerses = setOf(2, 3),
        )

        // When
        val isRead = chapter.isRangeRead(
            startVerse = 2,
            endVerse = null,
        )

        // Then
        assertFalse(isRead)
    }

    @Test
    fun `GIVEN no range WHEN every verse is read but the chapter flag is not THEN follows the chapter flag`() {
        // Given
        val chapter = chapter(
            isRead = false,
            readVerses = setOf(1, 2, 3, 4),
        )

        // When
        val isRead = chapter.isRangeRead(
            startVerse = null,
            endVerse = null,
        )

        // Then
        assertFalse(isRead)
    }

    private fun chapter(
        isRead: Boolean,
        readVerses: Set<Int>,
    ): BookChapterModel = BookChapterModel(
        number = 1,
        verses = (1..4).map { number ->
            VerseModel(
                number = number,
                isRead = number in readVerses,
            )
        },
        isRead = isRead,
        readUpdatedAt = null,
    )
}
