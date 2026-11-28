package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import com.quare.bibleplanner.core.model.book.BookId

data class BookFilterUiModel(
    val bookId: BookId?,
    val count: Int,
    val isSelected: Boolean,
)
