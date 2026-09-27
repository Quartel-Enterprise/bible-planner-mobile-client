package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class GetVerseReferenceUseCaseTest {
    @Test
    fun `formats the passage as chapter and compacted verse numbers after the book name`() = runTest {
        // When
        val reference = GetVerseReferenceUseCase()(
            bookId = BookId.PRO,
            chapterNumber = 3,
            verseNumbers = listOf(6, 5),
        )

        // Then
        assertTrue(reference.endsWith(" 3:5-6"), reference)
    }
}
