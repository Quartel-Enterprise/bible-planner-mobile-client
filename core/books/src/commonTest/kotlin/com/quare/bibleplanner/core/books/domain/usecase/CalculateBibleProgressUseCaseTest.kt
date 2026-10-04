package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.testing.FakeBooksRepository
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CalculateBibleProgressUseCaseTest {
    @Test
    fun `GIVEN a read chapter with incomplete verse flags WHEN calculating the progress THEN counts all its verses`() =
        runTest {
            // Given
            val book = BookDataModel(
                id = BookId.GEN,
                isRead = false,
                chapters = listOf(
                    BookChapterModel(
                        number = 1,
                        isRead = true,
                        verses = listOf(verse(1, isRead = true), verse(2, isRead = false)),
                        readUpdatedAt = null,
                    ),
                    BookChapterModel(
                        number = 2,
                        isRead = false,
                        verses = listOf(verse(1, isRead = true), verse(2, isRead = false)),
                        readUpdatedAt = null,
                    ),
                ),
                isFavorite = false,
            )
            val useCase = CalculateBibleProgressUseCase(FakeBooksRepository(listOf(book)))

            // When
            val progress = useCase().first()

            // Then
            assertEquals(75f, progress)
        }

    @Test
    fun `GIVEN no books WHEN calculating the progress THEN is zero`() = runTest {
        // Given
        val useCase = CalculateBibleProgressUseCase(FakeBooksRepository(emptyList()))

        // When
        val progress = useCase().first()

        // Then
        assertEquals(0f, progress)
    }

    private fun verse(
        number: Int,
        isRead: Boolean,
    ): VerseModel = VerseModel(number = number, isRead = isRead)
}
