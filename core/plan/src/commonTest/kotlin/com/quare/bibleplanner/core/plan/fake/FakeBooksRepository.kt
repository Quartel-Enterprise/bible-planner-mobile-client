package com.quare.bibleplanner.core.plan.fake

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeBooksRepository(
    books: List<BookDataModel>,
) : BooksRepository {
    val books = MutableStateFlow(books)
    val observedBookIds = mutableListOf<BookId>()

    override fun getBooksFlow(): Flow<List<BookDataModel>> = books

    override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> {
        observedBookIds += bookId
        return books.map { current -> current.find { it.id == bookId } }
    }

    override suspend fun getBooks(): List<BookDataModel> = books.value

    override suspend fun initializeDatabase() {
        error("Unexpected call")
    }

    override suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    ) {
        error("Unexpected call")
    }

    override fun getBookLayoutFormatFlow(): Flow<String?> = error("Unexpected call")

    override suspend fun setBookLayoutFormat(layoutFormat: String) {
        error("Unexpected call")
    }

    override fun getSelectedTestamentFlow(): Flow<String?> = error("Unexpected call")

    override suspend fun setSelectedTestament(testament: String) {
        error("Unexpected call")
    }
}
