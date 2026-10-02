package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.model.book.ChapterRef

internal data class ChapterStudyScope(
    val chapter: ChapterRef,
    val languageCode: String,
)
