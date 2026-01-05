package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel

sealed interface BooksUiEvent {
    data class OnSearchQueryChange(
        val query: String,
    ) : BooksUiEvent

    data object OnToggleCategorize : BooksUiEvent

    data class OnToggleGroupExpansion(
        val groupName: String,
    ) : BooksUiEvent

    data class OnBookClick(
        val book: BookPresentationModel,
    ) : BooksUiEvent

    data object OnToggleOnlyRead : BooksUiEvent

    data object OnToggleFavorites : BooksUiEvent

    data class OnToggleFavorite(
        val bookId: com.quare.bibleplanner.core.model.book.BookId,
    ) : BooksUiEvent
}
