package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.model.book.BookId

class ToggleBookFavoriteUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(
        bookId: BookId,
        isFavorite: Boolean,
    ) {
        booksRepository.updateBookFavoriteStatus(bookId, isFavorite)
    }
}
