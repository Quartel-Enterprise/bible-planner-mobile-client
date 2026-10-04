package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

/*
 * Why: rows are reset, never deleted, so chapter/verse/day reads with a remote row go pending
 * with a fresh timestamp and the reset reaches other devices. The book read flag is
 * local-only (derived from chapters), so it is reset without a push.
 */
class ResetAllProgressUseCase(
    private val dayDao: DayDao,
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke() {
        val now = currentTimestampProvider.getCurrentTimestamp()
        dayDao.resetAllDayMetaForSync(now)
        bookDao.resetAllBooksProgress()
        chapterDao.resetAllChapterReadsForSync(now)
        verseDao.resetAllVerseReadsForSync(now)
    }
}
