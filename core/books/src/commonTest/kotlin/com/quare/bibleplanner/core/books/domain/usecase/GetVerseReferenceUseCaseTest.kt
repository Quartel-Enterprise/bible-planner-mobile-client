package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class GetVerseReferenceUseCaseTest {
    @Test
    fun `GIVEN a passage WHEN getting its reference THEN puts the chapter and compacted verses after the book name`() =
        runTest {
            // Given
            val getVerseReference = GetVerseReferenceUseCase()

            // When
            val reference = getVerseReference(
                bookId = BookId.PRO,
                chapterNumber = 3,
                verseNumbers = listOf(6, 5),
            )

            // Then
            assertTrue(reference.endsWith(" 3:5-6"), reference)
        }
}
