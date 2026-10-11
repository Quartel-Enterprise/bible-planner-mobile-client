package com.quare.bibleplanner.core.chapterlistening.domain.repository

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioResultModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel

interface ChapterAudioRepository {
    suspend fun getChapterAudio(
        bibleVersionId: String,
        chapter: ChapterLocationModel,
    ): ChapterAudioResultModel

    suspend fun refresh(
        bibleVersionId: String,
        chapter: ChapterLocationModel,
    )

    suspend fun evict(audioPath: String)
}
