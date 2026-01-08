package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel

sealed interface BooksUiEvent {
    data class OnSearchQueryChange(
        val query: String,
    ) : BooksUiEvent

    data class OnTestamentSelect(
        val testament: com.quare.bibleplanner.feature.books.presentation.binding.BookTestament,
    ) : BooksUiEvent

    data class OnBookClick(
        val book: BookPresentationModel,
    ) : BooksUiEvent

    data class OnToggleFilter(
        val filterType: BookFilterType,
    ) : BooksUiEvent

    data class OnToggleFavorite(
        val bookId: com.quare.bibleplanner.core.model.book.BookId,
    ) : BooksUiEvent

    data class OnToggleFilterMenu(
        val isVisible: Boolean,
    ) : BooksUiEvent

    data object OnClearSearch : BooksUiEvent
}
