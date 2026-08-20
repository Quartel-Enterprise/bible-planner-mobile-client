package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.RemoveCustomHighlightColorUseCase
import com.quare.bibleplanner.core.verseannotations.fake.FakeHighlightPaletteRepository
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseHighlightRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val testChapter = ChapterRef(
    bibleVersionId = "ACF",
    bookId = BookId.GEN,
    chapterNumber = 3,
)

internal class RemoveCustomHighlightColorUseCaseTest {
    private val customColor = HighlightColor.Custom(
        hue = 265,
        lightness = 62,
    )
    private val highlightedRef = VerseRef(
        chapter = testChapter,
        verseNumber = 1,
    )
    private lateinit var useCase: RemoveCustomHighlightColorUseCase
    private lateinit var paletteRepository: FakeHighlightPaletteRepository
    private lateinit var highlightRepository: FakeVerseHighlightRepository

    @Test
    fun `drops the color from the palette but keeps the highlights made with it`() = runTest {
        // Given
        prepareScenario()

        // When
        useCase(
            colorKey = customColor.key,
            shouldKeepHighlights = true,
        )

        // Then
        assertTrue(paletteRepository.customColors.value.isEmpty())
        assertEquals(
            expected = mapOf(highlightedRef to customColor),
            actual = highlightRepository.colors.value,
        )
    }

    @Test
    fun `clears the highlights made with the color when the user asks for it`() = runTest {
        // Given
        prepareScenario()

        // When
        useCase(
            colorKey = customColor.key,
            shouldKeepHighlights = false,
        )

        // Then
        assertTrue(paletteRepository.customColors.value.isEmpty())
        assertEquals(
            expected = listOf(customColor.key),
            actual = highlightRepository.removedColorKeys,
        )
        assertTrue(highlightRepository.colors.value.isEmpty())
    }

    private fun prepareScenario() {
        paletteRepository = FakeHighlightPaletteRepository(initialColors = listOf(customColor))
        highlightRepository = FakeVerseHighlightRepository(
            initialColors = mapOf(highlightedRef to customColor),
        )
        useCase = RemoveCustomHighlightColorUseCase(
            highlightPaletteRepository = paletteRepository,
            verseHighlightRepository = highlightRepository,
        )
    }
}
