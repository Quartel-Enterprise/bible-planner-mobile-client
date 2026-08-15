package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ToggleSavedVersesUseCase
import com.quare.bibleplanner.core.verseannotations.fake.FakeSavedVerseRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ToggleSavedVersesUseCaseTest {
    private val refs = listOf(verseRef(1), verseRef(2))
    private lateinit var useCase: ToggleSavedVersesUseCase
    private lateinit var repository: FakeSavedVerseRepository

    @Test
    fun `saves the selection when it is not saved yet`() = runTest {
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
    fun `saves the whole selection when only part of it was saved`() = runTest {
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
    fun `unsaves the selection when all of it was already saved`() = runTest {
        // Given
        prepareScenario(initialSavedRefs = refs.toSet())

        // When
        val isSaved = useCase(refs)

        // Then
        assertFalse(isSaved)
        assertTrue(repository.savedRefs.value.isEmpty())
    }

    private fun verseRef(verseNumber: Int): VerseRef = VerseRef(
        bookId = BookId.GEN,
        chapterNumber = 3,
        verseNumber = verseNumber,
    )

    private fun prepareScenario(initialSavedRefs: Set<VerseRef> = emptySet()) {
        repository = FakeSavedVerseRepository(initialSavedRefs = initialSavedRefs)
        useCase = ToggleSavedVersesUseCase(savedVerseRepository = repository)
    }
}
