package com.quare.bibleplanner.core.chapterstudy.testing

import com.quare.bibleplanner.core.model.book.ChapterRef

data class ChapterStudyRequest(
    val chapter: ChapterRef,
    val languageCode: String,
)
