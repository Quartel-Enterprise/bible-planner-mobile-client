package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.data.repository.VerseSelectionRepositoryImpl
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ClearVerseSelectionUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ObserveVerseSelectionUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ToggleVerseSelectionUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ToggleVerseSelectionUseCaseTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private lateinit var toggleVerseSelection: ToggleVerseSelectionUseCase
    private lateinit var observeVerseSelection: ObserveVerseSelectionUseCase
    private lateinit var clearVerseSelection: ClearVerseSelectionUseCase

    @BeforeTest
    fun setUp() {
        val repository = VerseSelectionRepositoryImpl()
        toggleVerseSelection = ToggleVerseSelectionUseCase(repository)
        observeVerseSelection = ObserveVerseSelectionUseCase(repository)
        clearVerseSelection = ClearVerseSelectionUseCase(repository)
    }

    @Test
    fun `returns the selection after the toggle`() {
        // When
        val selection = toggleVerseSelection(
            chapter = testChapter,
            verseNumber = 2,
        )

        // Then
        assertEquals(
            expected = VerseSelection(
                chapter = testChapter,
                verseNumbers = listOf(2),
            ),
            actual = selection,
        )
    }

    @Test
    fun `exposes the toggled selection to its observers`() {
        // When
        toggleVerseSelection(
            chapter = testChapter,
            verseNumber = 2,
        )

        // Then
        assertEquals(
            expected = listOf(2),
            actual = observeVerseSelection().value?.verseNumbers,
        )
    }

    @Test
    fun `clearing drops the selection its observers see`() {
        // Given
        toggleVerseSelection(
            chapter = testChapter,
            verseNumber = 2,
        )

        // When
        clearVerseSelection()

        // Then
        assertNull(observeVerseSelection().value)
    }
}
