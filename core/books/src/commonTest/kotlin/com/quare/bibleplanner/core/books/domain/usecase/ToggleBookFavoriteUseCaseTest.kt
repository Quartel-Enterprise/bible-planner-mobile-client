package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ToggleBookFavoriteUseCaseTest {
    @Test
    fun `invoke forwards book id and favorite flag to repository`() = runTest {
        val repository = RecordingBooksRepository()
        val useCase = ToggleBookFavoriteUseCase(repository)

        useCase(BookId.GEN, isFavorite = true)

        assertEquals(BookId.GEN to true, repository.lastFavoriteUpdate)
    }

    private class RecordingBooksRepository : BooksRepository {
        var lastFavoriteUpdate: Pair<BookId, Boolean>? = null

        override suspend fun updateBookFavoriteStatus(
            bookId: BookId,
            isFavorite: Boolean,
        ) {
            lastFavoriteUpdate = bookId to isFavorite
        }

        override fun getBooksFlow(): Flow<List<BookDataModel>> = emptyFlow()

        override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> = emptyFlow()

        override suspend fun getBooks(): List<BookDataModel> = emptyList()

        override suspend fun initializeDatabase() = Unit

        override fun getBookLayoutFormatFlow(): Flow<String?> = emptyFlow()

        override suspend fun setBookLayoutFormat(layoutFormat: String) = Unit

        override fun getSelectedTestamentFlow(): Flow<String?> = emptyFlow()

        override suspend fun setSelectedTestament(testament: String) = Unit
    }
}
