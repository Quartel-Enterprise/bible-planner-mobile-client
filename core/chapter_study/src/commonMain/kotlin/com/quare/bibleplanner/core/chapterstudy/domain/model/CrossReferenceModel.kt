package com.quare.bibleplanner.core.chapterstudy.domain.model

import com.quare.bibleplanner.core.model.book.BookId

data class CrossReferenceModel(
    val bookId: BookId,
    val chapterNumber: Int,
    val startVerse: Int,
    val endVerse: Int,
)
