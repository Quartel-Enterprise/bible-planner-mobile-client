package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.provider.room.dao.BookDao

/**
 * Clears the user's local book read flag so a different account signing in on the same device never
 * inherits it. Book reference rows are preserved.
 *
 * Chapter, verse and day read state (plus day notes) are now synced datasets, so they are cleared by
 * the sync engine (`ClearAllSyncedLocalData`) instead of here — each dataset owns its own reset. The
 * book read flag is local-only (it derives from chapters), so it is reset here.
 */
class ClearLocalReadingDataUseCase(
    private val bookDao: BookDao,
) {
    suspend operator fun invoke() {
        bookDao.resetAllBooksProgress()
    }
}
