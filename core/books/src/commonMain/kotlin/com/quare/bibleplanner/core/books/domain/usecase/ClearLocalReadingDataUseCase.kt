package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.provider.room.dao.BookDao

// Why: chapter, verse and day read state are synced datasets cleared by
// ClearAllSyncedLocalData; the book flag is local-only and derived, so it is reset here.
class ClearLocalReadingDataUseCase(
    private val bookDao: BookDao,
) {
    suspend operator fun invoke() {
        bookDao.resetAllBooksProgress()
    }
}
