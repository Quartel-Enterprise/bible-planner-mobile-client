package com.quare.bibleplanner.core.verseannotations.domain.model

import com.quare.bibleplanner.core.model.book.ChapterRef

data class VerseRef(
    val chapter: ChapterRef,
    val verseNumber: Int,
)
