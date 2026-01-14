package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.core.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.core.model.book.BookId

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

    data object OnToggleFilterMenu : BooksUiEvent

    data object OnToggleSortMenu : BooksUiEvent

    data class OnSortOrderSelect(
        val sortOrder: BookSortOrder,
    ) : BooksUiEvent

    data object OnDismissSortMenu : BooksUiEvent

    data object OnDismissFilterMenu : BooksUiEvent

    data object OnDismissInformationBox : BooksUiEvent

    data object OnClearSearch : BooksUiEvent

    data class OnLayoutFormatSelect(
        val layoutFormat: BookLayoutFormat,
    ) : BooksUiEvent

    data object OnWebAppLinkClick : BooksUiEvent
}
