package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

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
