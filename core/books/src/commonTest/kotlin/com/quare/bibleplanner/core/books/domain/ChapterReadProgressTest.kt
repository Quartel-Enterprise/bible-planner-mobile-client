package com.quare.bibleplanner.core.books.domain

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
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

    @Test
    fun `GIVEN no stored book WHEN checking a plan chapter THEN it is not read`() {
        // Given
        val book: BookDataModel? = null
        val planChapter = planChapter(number = 1)

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertFalse(isRead)
    }

    @Test
    fun `GIVEN a book without the chapter number WHEN checking a plan chapter THEN it is not read`() {
        // Given
        val book = book(
            chapter(
                number = 1,
                isRead = true,
                readVerses = setOf(1, 2, 3, 4),
            ),
        )
        val planChapter = planChapter(number = 2)

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertFalse(isRead)
    }

    @Test
    fun `GIVEN several stored chapters WHEN checking one of them THEN only the chapter with that number counts`() {
        // Given
        val book = book(
            chapter(
                number = 1,
                isRead = true,
                readVerses = setOf(1, 2, 3, 4),
            ),
            chapter(
                number = 2,
                isRead = false,
                readVerses = emptySet(),
            ),
        )
        val planChapter = planChapter(number = 2)

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertFalse(isRead)
    }

    @Test
    fun `GIVEN a plan chapter without a verse range WHEN the stored chapter is flagged read THEN it is read`() {
        // Given
        val book = book(
            chapter(
                isRead = true,
                readVerses = emptySet(),
            ),
        )
        val planChapter = planChapter(number = 1)

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertTrue(isRead)
    }

    @Test
    fun `GIVEN a plan chapter without a verse range WHEN only the verses are read THEN it is not read`() {
        // Given
        val book = book(
            chapter(
                isRead = false,
                readVerses = setOf(1, 2, 3, 4),
            ),
        )
        val planChapter = planChapter(number = 1)

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertFalse(isRead)
    }

    @Test
    fun `GIVEN a bounded range WHEN every verse in it is read THEN the plan chapter is read`() {
        // Given
        val book = book(
            chapter(
                isRead = false,
                readVerses = setOf(2, 3),
            ),
        )
        val planChapter = planChapter(
            number = 1,
            startVerse = 2,
            endVerse = 3,
        )

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertTrue(isRead)
    }

    @Test
    fun `GIVEN a bounded range WHEN one verse in it is unread THEN the plan chapter is not read`() {
        // Given
        val book = book(
            chapter(
                isRead = false,
                readVerses = setOf(2),
            ),
        )
        val planChapter = planChapter(
            number = 1,
            startVerse = 2,
            endVerse = 3,
        )

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertFalse(isRead)
    }

    @Test
    fun `GIVEN a bounded range WHEN only verses outside it are read THEN the plan chapter is not read`() {
        // Given
        val book = book(
            chapter(
                isRead = false,
                readVerses = setOf(1, 4),
            ),
        )
        val planChapter = planChapter(
            number = 1,
            startVerse = 2,
            endVerse = 3,
        )

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertFalse(isRead)
    }

    @Test
    fun `GIVEN a bounded range WHEN verses outside it are unread but the range is read THEN it is read`() {
        // Given
        val book = book(
            chapter(
                isRead = false,
                readVerses = setOf(2, 3),
            ),
        )
        val planChapter = planChapter(
            number = 1,
            startVerse = 2,
            endVerse = 3,
        )

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertTrue(isRead)
    }

    @Test
    fun `GIVEN a bounded range WHEN the chapter is flagged read without verse flags THEN it is read`() {
        // Given
        val book = book(
            chapter(
                isRead = true,
                readVerses = emptySet(),
            ),
        )
        val planChapter = planChapter(
            number = 1,
            startVerse = 2,
            endVerse = 3,
        )

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertTrue(isRead)
    }

    @Test
    fun `GIVEN an open ended range WHEN every verse from the start is read THEN the plan chapter is read`() {
        // Given
        val book = book(
            chapter(
                isRead = false,
                readVerses = setOf(3, 4),
            ),
        )
        val planChapter = planChapter(
            number = 1,
            startVerse = 3,
        )

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertTrue(isRead)
    }

    @Test
    fun `GIVEN an open ended range WHEN a later verse is unread THEN the plan chapter is not read`() {
        // Given
        val book = book(
            chapter(
                isRead = false,
                readVerses = setOf(2, 3),
            ),
        )
        val planChapter = planChapter(
            number = 1,
            startVerse = 2,
        )

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertFalse(isRead)
    }

    @Test
    fun `GIVEN an open ended range WHEN the chapter is flagged read without verse flags THEN it is read`() {
        // Given
        val book = book(
            chapter(
                isRead = true,
                readVerses = emptySet(),
            ),
        )
        val planChapter = planChapter(
            number = 1,
            startVerse = 2,
        )

        // When
        val isRead = book.isChapterRead(planChapter)

        // Then
        assertTrue(isRead)
    }

    private fun book(vararg chapters: BookChapterModel): BookDataModel = BookDataModel(
        id = BookId.GEN,
        chapters = chapters.toList(),
        isRead = false,
        isFavorite = false,
    )

    private fun planChapter(
        number: Int,
        startVerse: Int? = null,
        endVerse: Int? = null,
    ): ChapterModel = ChapterModel(
        number = number,
        startVerse = startVerse,
        endVerse = endVerse,
        bookId = BookId.GEN,
    )

    private fun chapter(
        isRead: Boolean,
        readVerses: Set<Int>,
        number: Int = 1,
    ): BookChapterModel = BookChapterModel(
        number = number,
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
