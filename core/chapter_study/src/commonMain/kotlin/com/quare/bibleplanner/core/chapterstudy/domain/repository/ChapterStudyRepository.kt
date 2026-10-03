package com.quare.bibleplanner.core.chapterstudy.domain.repository

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationEventModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.model.book.ChapterRef
import kotlinx.coroutines.flow.Flow

interface ChapterStudyRepository {
    fun generateChapterStudy(
        chapter: ChapterRef,
        languageCode: String,
        isRewarded: Boolean,
    ): Flow<ChapterStudyGenerationEventModel>

    suspend fun fetchStatus(
        chapter: ChapterRef,
        languageCode: String,
    ): ChapterStudyStatusModel?

    suspend fun findCachedStudy(
        chapter: ChapterRef,
        languageCode: String,
    ): ChapterStudyModel?

    suspend fun clearCache()
}
