package com.quare.bibleplanner.feature.day.fake

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeBooksRepository(
    books: List<BookDataModel>,
) : BooksRepository {
    val books = MutableStateFlow(books)

    override fun getBooksFlow(): Flow<List<BookDataModel>> = books

    override fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?> = error("unused")

    override suspend fun getBooks(): List<BookDataModel> = error("unused")

    override suspend fun initializeDatabase() = error("unused")

    override suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    ) = error("unused")

    override fun getBookLayoutFormatFlow(): Flow<String?> = error("unused")

    override suspend fun setBookLayoutFormat(layoutFormat: String) = error("unused")

    override fun getSelectedTestamentFlow(): Flow<String?> = error("unused")

    override suspend fun setSelectedTestament(testament: String) = error("unused")
}
