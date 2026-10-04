package com.quare.bibleplanner.feature.read.presentation.screen

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.fixture.readChapter
import com.quare.bibleplanner.feature.read.presentation.model.VerseFocusUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class VerseItemIndexTest {
    private val chapters = listOf(
        readChapter(
            chapterNumber = 1,
            verseCount = 3,
        ),
        readChapter(
            chapterNumber = 2,
            verseCount = 4,
        ),
    )

    @Test
    fun `GIVEN the study card below WHEN finding a verse THEN skips earlier chapters with their study cards`() {
        // Given
        val focus = VerseFocusUiModel(
            bookId = BookId.GEN,
            chapterNumber = 2,
            verseNumbers = listOf(4, 3),
        )

        // When
        val index = findVerseItemIndex(
            chapters = chapters,
            leadingItemCount = 2,
            isChapterStudyBeside = false,
            focus = focus,
        )

        // Then
        assertEquals(
            expected = 2 + 6 + 1 + 2,
            actual = index,
        )
    }

    @Test
    fun `GIVEN the study card beside WHEN finding a verse THEN skips only earlier headers and end rows`() {
        // Given
        val focus = VerseFocusUiModel(
            bookId = BookId.GEN,
            chapterNumber = 2,
            verseNumbers = listOf(4, 3),
        )

        // When
        val index = findVerseItemIndex(
            chapters = chapters,
            leadingItemCount = 2,
            isChapterStudyBeside = true,
            focus = focus,
        )

        // Then
        assertEquals(
            expected = 2 + 5 + 1 + 2,
            actual = index,
        )
    }

    @Test
    fun `GIVEN a chapter or verse that is not laid out WHEN finding a verse THEN finds nothing`() {
        // Given
        val missingChapterFocus = VerseFocusUiModel(
            bookId = BookId.EXO,
            chapterNumber = 1,
            verseNumbers = listOf(1),
        )
        val missingVerseFocus = VerseFocusUiModel(
            bookId = BookId.GEN,
            chapterNumber = 1,
            verseNumbers = listOf(9),
        )
        val noVerseFocus = VerseFocusUiModel(
            bookId = BookId.GEN,
            chapterNumber = 1,
            verseNumbers = emptyList(),
        )

        // When
        val missingChapter = findVerseItemIndex(
            chapters = chapters,
            leadingItemCount = 0,
            isChapterStudyBeside = false,
            focus = missingChapterFocus,
        )
        val missingVerse = findVerseItemIndex(
            chapters = chapters,
            leadingItemCount = 0,
            isChapterStudyBeside = false,
            focus = missingVerseFocus,
        )
        val noVerse = findVerseItemIndex(
            chapters = chapters,
            leadingItemCount = 0,
            isChapterStudyBeside = false,
            focus = noVerseFocus,
        )

        // Then
        assertNull(missingChapter)
        assertNull(missingVerse)
        assertNull(noVerse)
    }
}
