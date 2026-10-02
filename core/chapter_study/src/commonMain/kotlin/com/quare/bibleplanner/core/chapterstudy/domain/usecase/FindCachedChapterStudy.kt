package com.quare.bibleplanner.core.chapterstudy.domain.usecase

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel

fun interface FindCachedChapterStudy {
    suspend operator fun invoke(target: ChapterStudyTargetModel): ChapterStudyModel?
}
