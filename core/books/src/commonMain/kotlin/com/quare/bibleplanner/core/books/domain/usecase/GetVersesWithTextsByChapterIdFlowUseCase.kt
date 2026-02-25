package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.provider.room.dao.VerseDao

class GetVersesWithTextsByChapterIdFlowUseCase(
    private val verseDao: VerseDao,
) {
    operator fun invoke(chapterId: Long) = verseDao.getVersesWithTextsByChapterIdFlow(chapterId)
}
