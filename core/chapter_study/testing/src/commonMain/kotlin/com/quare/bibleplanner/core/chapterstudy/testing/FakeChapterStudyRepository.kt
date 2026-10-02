package com.quare.bibleplanner.core.chapterstudy.testing

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationEventModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.model.book.ChapterRef
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeChapterStudyRepository(
    var cachedStudy: ChapterStudyModel?,
    var status: ChapterStudyStatusModel?,
    var events: List<ChapterStudyGenerationEventModel>,
) : ChapterStudyRepository {
    var eventsError: Throwable? = null
    var neverCompletes = false
    var cacheClearCount = 0
    val generationRequests = mutableListOf<ChapterStudyRequest>()
    val statusRequests = mutableListOf<ChapterStudyRequest>()
    val cacheLookups = mutableListOf<ChapterStudyRequest>()

    override fun generateChapterStudy(
        chapter: ChapterRef,
        languageCode: String,
    ): Flow<ChapterStudyGenerationEventModel> = flow {
        generationRequests += ChapterStudyRequest(
            chapter = chapter,
            languageCode = languageCode,
        )
        events.forEach { event -> emit(event) }
        eventsError?.let { error -> throw error }
        if (neverCompletes) awaitCancellation()
    }

    override suspend fun fetchStatus(
        chapter: ChapterRef,
        languageCode: String,
    ): ChapterStudyStatusModel? {
        statusRequests += ChapterStudyRequest(
            chapter = chapter,
            languageCode = languageCode,
        )
        return status
    }

    override suspend fun findCachedStudy(
        chapter: ChapterRef,
        languageCode: String,
    ): ChapterStudyModel? {
        cacheLookups += ChapterStudyRequest(
            chapter = chapter,
            languageCode = languageCode,
        )
        return cachedStudy
    }

    override suspend fun clearCache() {
        cacheClearCount++
    }
}
