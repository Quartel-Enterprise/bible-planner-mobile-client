package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.RefreshChapterStudyCache

internal class RefreshChapterStudyCacheUseCase(
    private val repository: ChapterStudyRepository,
    private val scopeResolver: ChapterStudyScopeResolver,
) : RefreshChapterStudyCache {
    override suspend fun invoke(target: ChapterStudyTargetModel) {
        val scope = scopeResolver.resolve(target)
        repository.fetchStatus(
            chapter = scope.chapter,
            languageCode = scope.languageCode,
        )
    }
}
