package com.quare.bibleplanner.feature.read.domain.model

import com.quare.bibleplanner.core.model.book.BookId

data class ReadNavigationSuggestionModel(
    val bookId: BookId,
    val chapterNumber: Int,
)
