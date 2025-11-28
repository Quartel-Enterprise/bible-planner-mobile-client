package com.quare.bibleplanner.core.books.domain.repository

import com.quare.bibleplanner.core.model.book.BookDataModel
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun getBooksFlow(): Flow<List<BookDataModel>>
    suspend fun getBooks(): List<BookDataModel>
    suspend fun initializeDatabase()
}
