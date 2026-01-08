package com.quare.bibleplanner.core.model.book

data class BookDataModel(
    val id: BookId,
    val chapters: List<BookChapterModel>,
    val isRead: Boolean,
    val isFavorite: Boolean = false,
)
