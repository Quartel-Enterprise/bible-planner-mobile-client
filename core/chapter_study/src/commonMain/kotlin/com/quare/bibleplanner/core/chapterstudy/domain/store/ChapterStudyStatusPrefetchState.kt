package com.quare.bibleplanner.core.chapterstudy.domain.store

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.ChapterStudyStatusKey

internal data class ChapterStudyStatusPrefetchState(
    val clearCount: Int,
    val statusesByKey: Map<ChapterStudyStatusKey, ChapterStudyStatusModel>,
)
