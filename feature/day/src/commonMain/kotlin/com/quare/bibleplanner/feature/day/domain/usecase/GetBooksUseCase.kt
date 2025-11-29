package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import kotlinx.coroutines.flow.Flow

class GetBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(): Flow<List<BookDataModel>> = booksRepository.getBooksFlow()
}

