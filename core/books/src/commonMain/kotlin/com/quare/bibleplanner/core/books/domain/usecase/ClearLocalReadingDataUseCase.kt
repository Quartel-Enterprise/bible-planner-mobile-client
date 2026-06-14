package com.quare.bibleplanner.core.books.domain.usecase

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.db.AppDatabase

/**
 * Clears the user's local reading progress stored in the database so a different account signing in
 * on the same device never inherits it: reading progress (books/chapters/verses/days), reading
 * timestamps, and day notes. Book reference rows are preserved.
 *
 * All writes run in a single immediate transaction so the database can never be left half-cleared.
 * The DAO calls join the transaction via Room's connection confinement (they reuse the writer
 * connection held by [useWriterConnection]).
 *
 * Synced data (favorites, reading-plan preferences) is cleared separately by the sync engine
 * (`ClearAllSyncedLocalData`) so each dataset owns its own reset.
 */
class ClearLocalReadingDataUseCase(
    private val appDatabase: AppDatabase,
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val dayDao: DayDao,
) {
    suspend operator fun invoke() {
        appDatabase.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                bookDao.resetAllBooksProgress()
                chapterDao.resetAllChaptersProgress()
                verseDao.resetAllVersesProgress()
                dayDao.resetAllDaysProgress()
                dayDao.clearAllDayNotes()
            }
        }
    }
}
