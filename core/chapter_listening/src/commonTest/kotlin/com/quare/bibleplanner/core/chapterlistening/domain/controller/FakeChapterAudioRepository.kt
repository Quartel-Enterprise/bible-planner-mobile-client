package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioFailureModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioResultModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterAudioRepository
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.CompletableDeferred

internal class FakeChapterAudioRepository(
    private val audioByChapter: Map<ChapterLocationModel, ChapterAudioModel>,
) : ChapterAudioRepository {
    var failure: ChapterAudioFailureModel? = null
    var gate: CompletableDeferred<Unit>? = null
    var failingChapters: Set<ChapterLocationModel> = emptySet()
    val requests = mutableListOf<ChapterLocationModel>()
    val refreshed = mutableListOf<ChapterLocationModel>()
    val evicted = mutableListOf<String>()

    override suspend fun loadChapterAudio(
        bibleVersionId: String,
        chapter: ChapterLocationModel,
    ): ChapterAudioResultModel {
        requests += chapter
        gate?.await()
        if (chapter in failingChapters) return ChapterAudioResultModel.Failed(ChapterAudioFailureModel.UNAVAILABLE)
        return failure?.let(ChapterAudioResultModel::Failed)
            ?: ChapterAudioResultModel.Ready(audioByChapter.getValue(chapter))
    }

    override suspend fun refresh(
        bibleVersionId: String,
        chapter: ChapterLocationModel,
    ) {
        refreshed += chapter
    }

    override suspend fun evict(audioPath: String) {
        evicted += audioPath
    }
}
