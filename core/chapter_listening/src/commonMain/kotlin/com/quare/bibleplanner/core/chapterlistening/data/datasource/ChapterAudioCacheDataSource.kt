package com.quare.bibleplanner.core.chapterlistening.data.datasource

import com.quare.bibleplanner.core.chapterlistening.data.model.CachedChapterAudio
import kotlinx.io.files.Path

internal interface ChapterAudioCacheDataSource {
    suspend fun find(chapterKey: String): CachedChapterAudio?

    suspend fun save(
        chapterKey: String,
        fileName: String,
        manifest: String,
        writeAudio: suspend (Path) -> Unit,
    ): CachedChapterAudio

    suspend fun remove(chapterKey: String)
}
