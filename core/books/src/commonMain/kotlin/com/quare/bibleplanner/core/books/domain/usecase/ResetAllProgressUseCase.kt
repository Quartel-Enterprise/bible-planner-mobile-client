package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

/**
 * Resets all reading progress by setting isRead to false for all entities
 * and clearing timestamps. Does not delete any rows.
 */
class ResetAllProgressUseCase(
    private val dayDao: DayDao,
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
) {
    suspend operator fun invoke() {
        dayDao.resetAllDaysProgress()
        bookDao.resetAllBooksProgress()
        chapterDao.resetAllChaptersProgress()
        verseDao.resetAllVersesProgress()
    }
}
