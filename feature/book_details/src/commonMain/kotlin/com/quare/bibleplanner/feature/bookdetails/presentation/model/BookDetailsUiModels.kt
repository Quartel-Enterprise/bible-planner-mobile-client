package com.quare.bibleplanner.feature.bookdetails.presentation.model

import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookId

sealed interface BookDetailsUiState {
    data object Loading : BookDetailsUiState

    data class Success(
        val id: BookId,
        val name: String,
        val synopsis: String,
        val chapters: List<BookChapterModel>,
        val progress: Float,
        val readChaptersCount: Int,
        val totalChaptersCount: Int,
        val areAllChaptersRead: Boolean,
        val isFavorite: Boolean,
        val bookGroup: BookGroup,
        val bookCategoryName: String,
        val isSynopsisExpanded: Boolean = false,
    ) : BookDetailsUiState
}

sealed interface BookDetailsUiEvent {
    data object OnBackClick : BookDetailsUiEvent

    data object OnToggleFavorite : BookDetailsUiEvent

    data object OnToggleSynopsisExpanded : BookDetailsUiEvent

    data object OnToggleAllChapters : BookDetailsUiEvent

    data class OnChapterClick(
        val chapterNumber: Int,
    ) : BookDetailsUiEvent
}
