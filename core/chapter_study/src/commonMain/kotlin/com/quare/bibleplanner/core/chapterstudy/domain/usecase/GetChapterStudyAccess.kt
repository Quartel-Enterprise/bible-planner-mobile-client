package com.quare.bibleplanner.core.chapterstudy.domain.usecase

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel

fun interface GetChapterStudyAccess {
    suspend operator fun invoke(target: ChapterStudyTargetModel): ChapterStudyAccessModel
}
