package com.quare.bibleplanner.core.chapterstudy.domain.usecase

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyQuotaModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel

fun interface GetChapterStudyQuota {
    suspend operator fun invoke(target: ChapterStudyTargetModel): ChapterStudyQuotaModel?
}
