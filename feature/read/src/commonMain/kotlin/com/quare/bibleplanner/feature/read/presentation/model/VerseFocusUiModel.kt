package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.book.BookId

data class VerseFocusUiModel(
    val bookId: BookId,
    val chapterNumber: Int,
    val verseNumbers: List<Int>,
)
