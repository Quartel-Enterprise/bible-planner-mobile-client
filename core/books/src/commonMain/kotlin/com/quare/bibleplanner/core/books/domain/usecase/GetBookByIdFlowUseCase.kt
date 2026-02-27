package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import kotlinx.coroutines.flow.Flow

class GetBookByIdFlowUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(bookId: BookId): Flow<BookDataModel?> = booksRepository.getBookByIdFlow(bookId)
}
