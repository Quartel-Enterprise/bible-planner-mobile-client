package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BooksRepository

class InitializeBooksIfNeeded(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke() {
        repository.run {
            if (getBooks().isEmpty()) {
                initializeDatabase()
            }
        }
    }
}
