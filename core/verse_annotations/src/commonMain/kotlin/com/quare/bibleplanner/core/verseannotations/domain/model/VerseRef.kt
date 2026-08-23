package com.quare.bibleplanner.core.verseannotations.domain.model

import com.quare.bibleplanner.core.model.book.ChapterRef

/** The address of a verse as the reader marked it: a verse of one chapter of one version. */
data class VerseRef(
    val chapter: ChapterRef,
    val verseNumber: Int,
)
