package com.quare.bibleplanner.feature.books.presentation.model

sealed interface BooksUiState {
    data object Loading : BooksUiState

    data class Success(
        val books: List<BookPresentationModel>,
        val filteredBooks: List<BookPresentationModel>,
        val selectedTestamentFilter: BookTestamentFilter,
        val searchQuery: String,
        val visibleGroups: List<BookGroupPresentationModel>,
        val filterOptions: List<BookFilterOption>,
        val shouldShowTestamentToggle: Boolean,
        val isFilterMenuVisible: Boolean,
        val isSortMenuVisible: Boolean,
        val isTestamentMenuVisible: Boolean,
        val sortOrder: BookSortOrder?,
        val layoutFormat: BookLayoutFormat,
    ) : BooksUiState
}
