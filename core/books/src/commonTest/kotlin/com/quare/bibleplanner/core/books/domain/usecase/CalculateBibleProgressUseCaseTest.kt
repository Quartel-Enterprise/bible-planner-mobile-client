package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
                ),
                BookChapterModel(
                    number = 2,
                    isRead = false,
                    verses = listOf(verse(1, isRead = true), verse(2, isRead = false)),
                ),
            ),
        )
        val useCase = CalculateBibleProgressUseCase(FakeBooksRepository(flowOf(listOf(book))))

        val progress = useCase().first()

        assertEquals(75f, progress)
    }

    @Test
    fun `progress is zero when there are no books`() = runTest {
        val useCase = CalculateBibleProgressUseCase(FakeBooksRepository(flowOf(emptyList())))

        assertEquals(0f, useCase().first())
    }

    private fun verse(
        number: Int,
        isRead: Boolean,
    ): VerseModel = VerseModel(number = number, isRead = isRead, text = null)

    private class FakeBooksRepository(
        private val booksFlow: Flow<List<BookDataModel>>,
    ) : BooksRepository {
        override fun getBooksFlow(): Flow<List<BookDataModel>> = booksFlow

        override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> = emptyFlow()

        override suspend fun getBooks(): List<BookDataModel> = emptyList()

        override suspend fun initializeDatabase() = Unit

        override suspend fun updateBookFavoriteStatus(
            bookId: BookId,
            isFavorite: Boolean,
        ) = Unit

        override fun getBookLayoutFormatFlow(): Flow<String?> = emptyFlow()

        override suspend fun setBookLayoutFormat(layoutFormat: String) = Unit

        override fun getSelectedTestamentFlow(): Flow<String?> = emptyFlow()

        override suspend fun setSelectedTestament(testament: String) = Unit
    }
}
