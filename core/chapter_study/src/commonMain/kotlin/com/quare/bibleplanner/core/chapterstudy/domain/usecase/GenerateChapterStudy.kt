package com.quare.bibleplanner.core.chapterstudy.domain.usecase

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationEventModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import kotlinx.coroutines.flow.Flow

fun interface GenerateChapterStudy {
    operator fun invoke(
        target: ChapterStudyTargetModel,
        isRewarded: Boolean,
    ): Flow<ChapterStudyGenerationEventModel>
}
