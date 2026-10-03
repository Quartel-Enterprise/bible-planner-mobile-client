package com.quare.bibleplanner.core.model.book

data class ChapterRef(
    val bibleVersionId: String,
    val bookId: BookId,
    val chapterNumber: Int,
)
