package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ToggleSavedVersesUseCase
import com.quare.bibleplanner.core.verseannotations.fake.FakeSavedVerseRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ToggleSavedVersesUseCaseTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private val refs = listOf(verseRef(1), verseRef(2))
    private lateinit var useCase: ToggleSavedVersesUseCase
    private lateinit var repository: FakeSavedVerseRepository

    @Test
    fun `GIVEN an unsaved selection WHEN toggling it THEN saves it`() = runTest {
        // Given
        prepareScenario()

        // When
        val isSaved = useCase(refs)

        // Then
        assertTrue(isSaved)
        assertEquals(
            expected = refs.toSet(),
            actual = repository.savedRefs.value,
        )
    }

    @Test
    fun `GIVEN a partly saved selection WHEN toggling it THEN saves the whole selection`() = runTest {
        // Given
        prepareScenario(initialSavedRefs = setOf(verseRef(1)))

        // When
        val isSaved = useCase(refs)

        // Then
        assertTrue(isSaved)
        assertEquals(
            expected = refs.toSet(),
            actual = repository.savedRefs.value,
        )
    }

    @Test
    fun `GIVEN a fully saved selection WHEN toggling it THEN unsaves it`() = runTest {
        // Given
        prepareScenario(initialSavedRefs = refs.toSet())

        // When
        val isSaved = useCase(refs)

        // Then
        assertFalse(isSaved)
        assertTrue(repository.savedRefs.value.isEmpty())
    }

    private fun verseRef(verseNumber: Int): VerseRef = VerseRef(
        chapter = testChapter,
        verseNumber = verseNumber,
    )

    private fun prepareScenario(initialSavedRefs: Set<VerseRef> = emptySet()) {
        repository = FakeSavedVerseRepository(initialSavedRefs = initialSavedRefs)
        useCase = ToggleSavedVersesUseCase(savedVerseRepository = repository)
    }
}
