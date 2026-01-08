package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament

sealed interface BooksUiEvent {
    data class OnSearchQueryChange(
        val query: String,
    ) : BooksUiEvent

    data class OnTestamentSelect(
        val testament: BookTestament,
    ) : BooksUiEvent

    data class OnBookClick(
        val book: BookPresentationModel,
    ) : BooksUiEvent

    data class OnToggleFilter(
        val filterType: BookFilterType,
    ) : BooksUiEvent

    data class OnToggleFavorite(
        val bookId: BookId,
    ) : BooksUiEvent

    data class OnToggleFilterMenu(
        val isVisible: Boolean,
    ) : BooksUiEvent

    data class OnToggleSortMenu(
        val isVisible: Boolean,
    ) : BooksUiEvent

    data class OnSortOrderSelect(
        val sortOrder: BookSortOrder,
    ) : BooksUiEvent

    data object OnDismissSortMenu : BooksUiEvent

    data object OnDismissFilterMenu : BooksUiEvent

    data object OnClearSearch : BooksUiEvent
}
