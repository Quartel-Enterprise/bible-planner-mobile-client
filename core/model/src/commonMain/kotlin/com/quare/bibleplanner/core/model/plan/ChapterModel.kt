package com.quare.bibleplanner.core.model.plan

import com.quare.bibleplanner.core.model.book.BookId

data class ChapterModel(
    val number: Int,
    val startVerse: Int?,
    val endVerse: Int?,
    val bookId: BookId,
)
