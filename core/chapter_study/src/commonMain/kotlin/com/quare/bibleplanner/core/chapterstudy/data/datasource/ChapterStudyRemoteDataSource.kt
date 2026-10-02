package com.quare.bibleplanner.core.chapterstudy.data.datasource

import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyRequestDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyStatusDto
import com.quare.bibleplanner.core.chapterstudy.data.model.ChapterStudyStreamEvent
import kotlinx.coroutines.flow.Flow

internal interface ChapterStudyRemoteDataSource {
    fun streamChapterStudy(request: ChapterStudyRequestDto): Flow<ChapterStudyStreamEvent>

    suspend fun fetchStatus(request: ChapterStudyRequestDto): Result<ChapterStudyStatusDto>
}
