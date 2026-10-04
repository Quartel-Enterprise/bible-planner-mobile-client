package com.quare.bibleplanner.core.chapterstudy.domain.usecase

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel

fun interface PrefetchChapterStudyStatus {
    suspend operator fun invoke(target: ChapterStudyTargetModel)
}
