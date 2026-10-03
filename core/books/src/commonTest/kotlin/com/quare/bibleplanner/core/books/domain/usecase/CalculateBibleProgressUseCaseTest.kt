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
    fun `a chapter flagged read counts all its verses even when verse flags are incomplete`() = runTest {
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

        val progress = useCase().first()

        assertEquals(75f, progress)
    }

    @Test
    fun `progress is zero when there are no books`() = runTest {
        val useCase = CalculateBibleProgressUseCase(FakeBooksRepository(emptyList()))

        assertEquals(0f, useCase().first())
    }

    private fun verse(
        number: Int,
        isRead: Boolean,
    ): VerseModel = VerseModel(number = number, isRead = isRead)
}
