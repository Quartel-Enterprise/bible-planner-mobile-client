package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import kotlinx.coroutines.flow.Flow

class GetBooksFlowUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(): Flow<List<BookDataModel>> = booksRepository.getBooksFlow()
}
