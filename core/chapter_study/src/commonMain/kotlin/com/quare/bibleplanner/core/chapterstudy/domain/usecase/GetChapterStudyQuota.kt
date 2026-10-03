package com.quare.bibleplanner.core.chapterstudy.domain.usecase

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyQuotaModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel

// Why: null means there is no quota to show (logged out, or the status could not be read).
fun interface GetChapterStudyQuota {
    suspend operator fun invoke(target: ChapterStudyTargetModel): ChapterStudyQuotaModel?
}
