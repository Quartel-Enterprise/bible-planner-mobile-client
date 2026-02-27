package com.quare.bibleplanner.core.model.plan

import com.quare.bibleplanner.core.model.book.BookId

data class PassageModel(
    val bookId: BookId,
    val chapters: List<ChapterModel>,
    val isRead: Boolean,
    val chapterRanges: String?,
)
