package com.quare.bibleplanner.core.verseannotations.domain.model

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import kotlin.test.Test
import kotlin.test.assertEquals

internal class VerseSelectionTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.GEN,
        chapterNumber = 3,
    )

    @Test
    fun `addresses every selected verse within its chapter`() {
        // Given
        val selection = VerseSelection(
            chapter = testChapter,
            verseNumbers = listOf(1, 4),
        )

        // When
        val refs = selection.refs

        // Then
        assertEquals(
            expected = listOf(
                VerseRef(
                    chapter = testChapter,
                    verseNumber = 1,
                ),
                VerseRef(
                    chapter = testChapter,
                    verseNumber = 4,
                ),
            ),
            actual = refs,
        )
    }
}
