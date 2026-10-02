package com.quare.bibleplanner.core.chapterstudy.domain.model

import com.quare.bibleplanner.core.model.book.BookId

data class ChapterStudyTargetModel(
    val bookId: BookId,
    val chapterNumber: Int,
)
