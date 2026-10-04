package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class VerseSelectionRepositoryImplTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.GEN,
        chapterNumber = 3,
    )

    private lateinit var repository: VerseSelectionRepositoryImpl

    @BeforeTest
    fun setUp() {
        repository = VerseSelectionRepositoryImpl()
    }

    @Test
    fun `GIVEN a new repository WHEN reading the selection THEN nothing is selected`() {
        // When
        val selection = repository.selection.value

        // Then
        assertNull(selection)
    }

    @Test
    fun `GIVEN a later verse selected WHEN tapping an earlier verse THEN keeps the selection sorted`() {
        // Given
        toggle(3)

        // When
        toggle(1)

        // Then
        assertEquals(
            expected = listOf(1, 3),
            actual = repository.selection.value?.verseNumbers,
        )
    }

    @Test
    fun `GIVEN two selected verses WHEN tapping one again THEN drops it`() {
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
    fun `GIVEN a single selected verse WHEN deselecting it THEN clears the selection`() {
        // Given
        toggle(1)

        // When
        val selection = toggle(1)

        // Then
        assertNull(selection)
        assertNull(repository.selection.value)
    }

    @Test
    fun `GIVEN selected verses WHEN tapping a verse of another chapter THEN starts over`() {
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
    fun `GIVEN selected verses WHEN tapping the same verse in another version THEN starts over`() {
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
    fun `GIVEN a selected verse WHEN clearing THEN empties the selection`() {
        // Given
        toggle(1)

        // When
        repository.clear()

        // Then
        assertNull(repository.selection.value)
    }

    private fun toggle(verseNumber: Int): VerseSelection? = repository.toggle(
        chapter = testChapter,
        verseNumber = verseNumber,
    )
}
