package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.clear.domain.ClearChapterStudyLocalData

internal class ClearChapterStudyLocalDataUseCase(
    private val repository: ChapterStudyRepository,
) : ClearChapterStudyLocalData {
    override suspend fun invoke() {
        repository.clearCache()
    }
}
