package com.quare.bibleplanner.core.chapterlistening.data.datasource

import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioRequestDto
import com.quare.bibleplanner.core.chapterlistening.data.dto.ChapterAudioResponseDto
import kotlinx.io.files.Path

internal interface ChapterAudioRemoteDataSource {
    suspend fun fetchChapterAudio(request: ChapterAudioRequestDto): ChapterAudioResponseDto

    suspend fun downloadText(url: String): String

    suspend fun downloadFile(
        url: String,
        destination: Path,
    )
}
