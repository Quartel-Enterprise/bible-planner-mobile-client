package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ApplyHighlightColorUseCase
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseHighlightRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val testChapter = ChapterRef(
    bibleVersionId = "ACF",
    bookId = BookId.GEN,
    chapterNumber = 3,
)

internal class ApplyHighlightColorUseCaseTest {
    private val yellow = HighlightColor.Preset(PresetHighlightColor.YELLOW)
    private val green = HighlightColor.Preset(PresetHighlightColor.GREEN)
    private val refs = listOf(verseRef(1), verseRef(2))
    private lateinit var useCase: ApplyHighlightColorUseCase
    private lateinit var repository: FakeVerseHighlightRepository

    @Test
    fun `applies the color to every selected verse when none of them has it`() = runTest {
        // Given
        prepareScenario()

        // When
        val isApplied = useCase(
            refs = refs,
            color = yellow,
        )

        // Then
        assertTrue(isApplied)
        assertEquals(
            expected = refs.associateWith { yellow },
            actual = repository.colors.value,
        )
    }

    @Test
    fun `applies the color when only part of the selection already has it`() = runTest {
        // Given
        prepareScenario(initialColors = mapOf(verseRef(1) to yellow))

        // When
        val isApplied = useCase(
            refs = refs,
            color = yellow,
        )

        // Then
        assertTrue(isApplied)
        assertEquals(
            expected = refs.associateWith { yellow },
            actual = repository.colors.value,
        )
    }

    @Test
    fun `clears the highlight when every selected verse already has that exact color`() = runTest {
        // Given
        prepareScenario(initialColors = refs.associateWith { yellow })

        // When
        val isApplied = useCase(
            refs = refs,
            color = yellow,
        )

        // Then
        assertFalse(isApplied)
        assertTrue(repository.colors.value.isEmpty())
    }

    @Test
    fun `replaces a different color instead of clearing it`() = runTest {
        // Given
        prepareScenario(initialColors = refs.associateWith { green })

        // When
        useCase(
            refs = refs,
            color = yellow,
        )

        // Then
        assertEquals(
            expected = refs.associateWith { yellow },
            actual = repository.colors.value,
        )
        assertNull(
            repository.colors.value.values
                .find { it == green },
        )
    }

    private fun verseRef(verseNumber: Int): VerseRef = VerseRef(
        chapter = testChapter,
        verseNumber = verseNumber,
    )

    private fun prepareScenario(initialColors: Map<VerseRef, HighlightColor> = emptyMap()) {
        repository = FakeVerseHighlightRepository(initialColors = initialColors)
        useCase = ApplyHighlightColorUseCase(verseHighlightRepository = repository)
    }
}
