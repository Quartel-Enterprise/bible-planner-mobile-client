package com.quare.bibleplanner.feature.readingplan.domain.model

import com.quare.bibleplanner.core.model.book.BookId

internal data class ResumeReadTarget(
    val bookId: BookId,
    val chapterNumber: Int,
    val isChapterRead: Boolean,
)
