package com.quare.bibleplanner.core.books.fake

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
    val layoutFormat = MutableStateFlow<String?>(null)
    val selectedTestament = MutableStateFlow<String?>(null)
    var initializationCount = 0
        private set

    override fun getBooksFlow(): Flow<List<BookDataModel>> = books

    override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> =
        books.map { current -> current.find { it.id == bookId } }

    override suspend fun getBooks(): List<BookDataModel> = books.value

    override suspend fun initializeDatabase() {
        initializationCount += 1
    }

    override suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    ) {
        error("Unexpected call")
    }

    override fun getBookLayoutFormatFlow(): Flow<String?> = layoutFormat

    override suspend fun setBookLayoutFormat(layoutFormat: String) {
        this.layoutFormat.value = layoutFormat
    }

    override fun getSelectedTestamentFlow(): Flow<String?> = selectedTestament

    override suspend fun setSelectedTestament(testament: String) {
        selectedTestament.value = testament
    }
}
