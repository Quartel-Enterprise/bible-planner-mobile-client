package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.feature.books.presentation.binding.BookGroup
import com.quare.bibleplanner.feature.books.presentation.binding.BookTestament

sealed interface BooksUiState {
    data object Loading : BooksUiState

    data class Success(
        val books: List<BookPresentationModel>,
        val filteredBooks: List<BookPresentationModel>,
        val isCategorized: Boolean,
        val searchQuery: String,
        val expandedGroups: Set<String> = emptySet(), // Stores the names/IDs of expanded groups
        val categorizedBooks: Map<BookTestament, Map<BookGroup, List<BookPresentationModel>>> = emptyMap(),
        val isOnlyRead: Boolean = false,
        val isFavoritesOnly: Boolean = false,
    ) : BooksUiState
}
