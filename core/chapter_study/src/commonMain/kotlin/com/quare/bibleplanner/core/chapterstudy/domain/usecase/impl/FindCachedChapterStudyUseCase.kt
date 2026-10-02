package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.FindCachedChapterStudy

internal class FindCachedChapterStudyUseCase(
    private val repository: ChapterStudyRepository,
    private val scopeResolver: ChapterStudyScopeResolver,
) : FindCachedChapterStudy {
    override suspend fun invoke(target: ChapterStudyTargetModel): ChapterStudyModel? {
        val scope = scopeResolver.resolve(target)
        return repository.findCachedStudy(
            chapter = scope.chapter,
            languageCode = scope.languageCode,
        )
    }
}
