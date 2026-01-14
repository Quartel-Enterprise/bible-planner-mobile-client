package com.quare.bibleplanner.feature.books.presentation.model

sealed interface BooksUiAction {
    data object ScrollToTop : BooksUiAction

    data class OpenWebAppLink(
        val url: String,
    ) : BooksUiAction

    data class ShowReadingNotAvailableYetSnackbar(
        val url: String,
    ) : BooksUiAction

    data class NavigateToBookDetails(
        val bookId: String,
    ) : BooksUiAction
}
