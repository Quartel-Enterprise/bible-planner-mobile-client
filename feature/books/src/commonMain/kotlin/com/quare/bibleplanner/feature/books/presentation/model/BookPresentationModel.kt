package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.core.model.book.BookId

data class BookPresentationModel(
    val id: BookId,
    val name: String,
    val chapterProgressText: String,
    val progress: Float,
    val percentageText: String,
    val isCompleted: Boolean,
    val isFavorite: Boolean,
)
