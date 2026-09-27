package com.quare.bibleplanner.feature.read.presentation.screen

import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseFocusUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class VerseItemIndexTest {
    private val chapters = listOf(
        chapter(
            chapterNumber = 1,
            verseCount = 3,
        ),
        chapter(
            chapterNumber = 2,
            verseCount = 4,
        ),
    )

    @Test
    fun `points at the first focused verse after the leading items and the earlier chapters and the header`() {
        // When
        val index = findVerseItemIndex(
            chapters = chapters,
            leadingItemCount = 2,
            focus = VerseFocusUiModel(
                bookId = BookId.GEN,
                chapterNumber = 2,
                verseNumbers = listOf(4, 3),
            ),
        )

        // Then
        assertEquals(
            expected = 2 + 5 + 1 + 2,
            actual = index,
        )
    }

    @Test
    fun `finds nothing for a chapter or verse that is not laid out`() {
        // When
        val missingChapter = findVerseItemIndex(
            chapters = chapters,
            leadingItemCount = 0,
            focus = VerseFocusUiModel(
                bookId = BookId.EXO,
                chapterNumber = 1,
                verseNumbers = listOf(1),
            ),
        )
        val missingVerse = findVerseItemIndex(
            chapters = chapters,
            leadingItemCount = 0,
            focus = VerseFocusUiModel(
                bookId = BookId.GEN,
                chapterNumber = 1,
                verseNumbers = listOf(9),
            ),
        )
        val noVerse = findVerseItemIndex(
            chapters = chapters,
            leadingItemCount = 0,
            focus = VerseFocusUiModel(
                bookId = BookId.GEN,
                chapterNumber = 1,
                verseNumbers = emptyList(),
            ),
        )

        // Then
        assertNull(missingChapter)
        assertNull(missingVerse)
        assertNull(noVerse)
    }

    private fun chapter(
        chapterNumber: Int,
        verseCount: Int,
    ): ReadChapterUiModel = ReadChapterUiModel(
        chapter = ChapterRef(
            bibleVersionId = "WEB",
            bookId = BookId.GEN,
            chapterNumber = chapterNumber,
        ),
        bookStringResource = BookId.GEN.toBookNameResource(),
        isRead = false,
        verses = (1..verseCount).map { number ->
            VerseUiModel(
                number = number,
                heading = null,
                text = "Verse $number",
                isSelected = false,
                highlightColor = null,
                isSaved = false,
                noteId = null,
            )
        },
    )
}
