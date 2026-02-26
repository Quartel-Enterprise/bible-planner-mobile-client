package com.quare.bibleplanner.core.books.domain.repository

import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun getBooksFlow(): Flow<List<BookDataModel>>

    fun getBookByIdFlow(bookId: BookId): Flow<BookDataModel?>

    suspend fun getBooks(): List<BookDataModel>

    suspend fun initializeDatabase()

    suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    )

    fun getBookLayoutFormatFlow(): Flow<String?>

    suspend fun setBookLayoutFormat(layoutFormat: String)

    fun getSelectedTestamentFlow(): Flow<String?>

    suspend fun setSelectedTestament(testament: String)
}
