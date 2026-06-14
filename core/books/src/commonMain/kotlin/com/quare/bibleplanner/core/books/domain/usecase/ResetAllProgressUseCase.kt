package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

/**
 * Resets all reading progress by setting isRead to false for all entities and clearing read
 * timestamps. Does not delete any rows. Chapter/verse/day read state is synced, so the reset is
 * scheduled for push (rows that already had a remote row are flagged pending with a fresh timestamp)
 * and propagates to the user's other devices. The book read flag is local-only (it derives from
 * chapters) and is reset without a push.
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
