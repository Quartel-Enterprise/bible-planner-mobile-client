package com.quare.bibleplanner.feature.books.presentation.model

sealed interface BooksUiAction {
    data object ScrollToTop : BooksUiAction
}
