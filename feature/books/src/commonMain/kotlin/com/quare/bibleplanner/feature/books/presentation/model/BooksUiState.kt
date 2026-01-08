package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament

sealed interface BooksUiState {
    data object Loading : BooksUiState

    data class Success(
        val books: List<BookPresentationModel>,
        val filteredBooks: List<BookPresentationModel>,
        val selectedTestament: BookTestament,
        val searchQuery: String,
        val groupsInTestament: List<BookGroupPresentationModel>,
        val filterOptions: List<BookFilterOption>,
        val shouldShowTestamentToggle: Boolean,
        val isFilterMenuVisible: Boolean,
    ) : BooksUiState
}
