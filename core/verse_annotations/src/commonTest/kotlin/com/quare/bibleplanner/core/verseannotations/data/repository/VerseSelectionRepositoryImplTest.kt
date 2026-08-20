package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private val testChapter = ChapterRef(
    bibleVersionId = "ACF",
    bookId = BookId.GEN,
    chapterNumber = 3,
)

internal class VerseSelectionRepositoryImplTest {
    private lateinit var repository: VerseSelectionRepositoryImpl

    @BeforeTest
    fun setUp() {
        repository = VerseSelectionRepositoryImpl()
    }

    @Test
    fun `starts with nothing selected`() {
        // Then
        assertNull(repository.selection.value)
    }

    @Test
    fun `keeps the selection sorted whatever order the verses are tapped in`() {
        // When
        toggle(3)
        toggle(1)

        // Then
        assertEquals(
            expected = listOf(1, 3),
            actual = repository.selection.value?.verseNumbers,
        )
    }

    @Test
    fun `drops a verse that is tapped again`() {
        // Given
        toggle(1)
        toggle(2)

        // When
        toggle(1)

        // Then
        assertEquals(
            expected = listOf(2),
            actual = repository.selection.value?.verseNumbers,
        )
    }

    @Test
    fun `clears itself once the last verse is deselected`() {
        // Given
        toggle(1)

        // When
        val selection = toggle(1)

        // Then
        assertNull(selection)
        assertNull(repository.selection.value)
    }

    @Test
    fun `starts over when a verse of another chapter is tapped`() {
        // Given
        toggle(1)
        toggle(2)

        // When
        repository.toggle(
            chapter = testChapter.copy(chapterNumber = 4),
            verseNumber = 7,
        )

        // Then
        assertEquals(
            expected = 4,
            actual = repository.selection.value
                ?.chapter
                ?.chapterNumber,
        )
        assertEquals(
            expected = listOf(7),
            actual = repository.selection.value?.verseNumbers,
        )
    }

    @Test
    fun `starts over when the same verse is tapped in another version`() {
        // Given
        toggle(1)
        toggle(2)

        // When
        repository.toggle(
            chapter = testChapter.copy(bibleVersionId = "WEB"),
            verseNumber = 2,
        )

        // Then
        assertEquals(
            expected = "WEB",
            actual = repository.selection.value
                ?.chapter
                ?.bibleVersionId,
        )
        assertEquals(
            expected = listOf(2),
            actual = repository.selection.value?.verseNumbers,
        )
    }

    @Test
    fun `clear empties the selection`() {
        // Given
        toggle(1)

        // When
        repository.clear()

        // Then
        assertNull(repository.selection.value)
    }

    private fun toggle(verseNumber: Int) = repository.toggle(
        chapter = testChapter,
        verseNumber = verseNumber,
    )
}
