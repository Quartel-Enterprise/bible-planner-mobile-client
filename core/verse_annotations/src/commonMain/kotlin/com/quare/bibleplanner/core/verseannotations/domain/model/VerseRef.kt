package com.quare.bibleplanner.core.verseannotations.domain.model

import com.quare.bibleplanner.core.model.book.BookId

/** Canonical coordinates of a verse, stable across bible versions and devices. */
data class VerseRef(
    val bookId: BookId,
    val chapterNumber: Int,
    val verseNumber: Int,
)
