package com.quare.bibleplanner.core.chapterstudy.domain.usecase

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyQuotaModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel

/** `null` when there is no quota to show: the user is logged out, or the status couldn't be read. */
fun interface GetChapterStudyQuota {
    suspend operator fun invoke(target: ChapterStudyTargetModel): ChapterStudyQuotaModel?
}
