package com.quare.bibleplanner.core.model.book

// Why: the version is part of the identity because translations split and merge verses,
// so the same book/chapter coordinates do not point at the same words everywhere.
data class ChapterRef(
    val bibleVersionId: String,
    val bookId: BookId,
    val chapterNumber: Int,
)
