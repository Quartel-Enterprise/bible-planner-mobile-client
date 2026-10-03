package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationEventModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GenerateChapterStudy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

internal class GenerateChapterStudyUseCase(
    private val repository: ChapterStudyRepository,
    private val scopeResolver: ChapterStudyScopeResolver,
) : GenerateChapterStudy {
    override fun invoke(
        target: ChapterStudyTargetModel,
        isRewarded: Boolean,
    ): Flow<ChapterStudyGenerationEventModel> = flow {
        val scope = scopeResolver.resolve(target)
        emitAll(
            repository.generateChapterStudy(
                chapter = scope.chapter,
                languageCode = scope.languageCode,
                isRewarded = isRewarded,
            ),
        )
    }
}
