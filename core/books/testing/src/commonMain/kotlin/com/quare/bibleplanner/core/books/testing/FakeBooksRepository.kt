package com.quare.bibleplanner.core.books.testing

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBooksRepository(
    books: List<BookDataModel>,
) : BooksRepository {
    val books = MutableStateFlow(books)
    val layoutFormat = MutableStateFlow<String?>(null)
    val selectedTestament = MutableStateFlow<String?>(null)
    val observedBookIds = mutableListOf<BookId>()
    val favoriteUpdates = mutableListOf<Pair<BookId, Boolean>>()
    val layoutFormats = mutableListOf<String>()
    val selectedTestaments = mutableListOf<String>()
    var initializationCount = 0
        private set

    override fun getBooksFlow(): Flow<List<BookDataModel>> = books

    override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> {
        observedBookIds += bookId
        return books.map { current -> current.find { book -> book.id == bookId } }
    }

    override suspend fun getBooks(): List<BookDataModel> = books.value

    override suspend fun isDatabaseInitialized(): Boolean = books.value.isNotEmpty()

    override suspend fun initializeDatabase() {
        initializationCount += 1
    }

    override suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    ) {
        favoriteUpdates += bookId to isFavorite
        books.value = books.value.map { book -> if (book.id == bookId) book.copy(isFavorite = isFavorite) else book }
    }

    override fun getBookLayoutFormatFlow(): Flow<String?> = layoutFormat

    override suspend fun setBookLayoutFormat(layoutFormat: String) {
        layoutFormats += layoutFormat
        this.layoutFormat.value = layoutFormat
    }

    override fun getSelectedTestamentFlow(): Flow<String?> = selectedTestament

    override suspend fun setSelectedTestament(testament: String) {
        selectedTestaments += testament
        selectedTestament.value = testament
    }
}
