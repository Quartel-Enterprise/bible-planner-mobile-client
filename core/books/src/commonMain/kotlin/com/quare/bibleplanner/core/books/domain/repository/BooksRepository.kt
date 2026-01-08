package com.quare.bibleplanner.core.books.domain.repository

import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun getBooksFlow(): Flow<List<BookDataModel>>

    suspend fun getBooks(): List<BookDataModel>

    suspend fun initializeDatabase()

    fun getShowInformationBoxFlow(): Flow<Boolean>

    suspend fun setInformationBoxDismissed()

    suspend fun updateBookFavoriteStatus(
        bookId: BookId,
        isFavorite: Boolean,
    )
}
