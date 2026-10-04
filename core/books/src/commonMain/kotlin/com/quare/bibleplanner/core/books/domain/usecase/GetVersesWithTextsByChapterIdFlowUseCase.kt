package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import kotlinx.coroutines.flow.Flow

class GetVersesWithTextsByChapterIdFlowUseCase(
    private val verseDao: VerseDao,
) {
    operator fun invoke(chapterId: Long): Flow<List<VerseWithTexts>> =
        verseDao.getVersesWithTextsByChapterIdFlow(chapterId)
}
