package com.quare.bibleplanner.feature.books.fixture

import com.quare.bibleplanner.core.model.book.BookId

internal data class SampleBook(
    val id: BookId,
    val totalChapters: Int,
    val chaptersRead: Int,
    val isFavorite: Boolean,
)
