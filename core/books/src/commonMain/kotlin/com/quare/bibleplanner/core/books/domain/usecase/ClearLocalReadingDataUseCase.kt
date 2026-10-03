package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.provider.room.dao.BookDao

class ClearLocalReadingDataUseCase(
    private val bookDao: BookDao,
) {
    suspend operator fun invoke() {
        bookDao.resetAllBooksProgress()
    }
}
