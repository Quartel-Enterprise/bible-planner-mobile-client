package com.quare.bibleplanner.feature.books.fixture

import bibleplanner.feature.books.generated.resources.Res
import bibleplanner.feature.books.generated.resources.favorites
import bibleplanner.feature.books.generated.resources.read
import bibleplanner.feature.books.generated.resources.unread
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterOption
import com.quare.bibleplanner.feature.books.presentation.model.BookFilterType
import com.quare.bibleplanner.feature.books.presentation.model.BookGroupPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BookLayoutFormat
import com.quare.bibleplanner.feature.books.presentation.model.BookPresentationModel
import com.quare.bibleplanner.feature.books.presentation.model.BooksUiState

internal val genesis = BookPresentationModel(
    id = BookId.GEN,
    name = "Genesis",
    chapterProgressText = "50/50",
    progress = 1f,
    percentageText = "100%",
    isCompleted = true,
    isFavorite = true,
)

internal val exodus = BookPresentationModel(
    id = BookId.EXO,
    name = "Exodus",
    chapterProgressText = "20/40",
    progress = 0.5f,
    percentageText = "50%",
    isCompleted = false,
    isFavorite = false,
)

internal val joshua = BookPresentationModel(
    id = BookId.JOS,
    name = "Joshua",
    chapterProgressText = "0/24",
    progress = 0f,
    percentageText = "0%",
    isCompleted = false,
    isFavorite = false,
)

internal val booksFilterOptions = listOf(
    BookFilterOption(
        type = BookFilterType.OnlyRead,
        label = Res.string.read,
        isSelected = false,
    ),
    BookFilterOption(
        type = BookFilterType.OnlyUnread,
        label = Res.string.unread,
        isSelected = false,
    ),
    BookFilterOption(
        type = BookFilterType.Favorites,
        label = Res.string.favorites,
        isSelected = false,
    ),
)

internal fun booksUiState(): BooksUiState.Success {
    val pentateuch = listOf(genesis, exodus)
    val historical = listOf(joshua)
    return BooksUiState.Success(
        books = pentateuch + historical,
        filteredBooks = pentateuch + historical,
        selectedTestament = BookTestament.OldTestament,
        searchQuery = "",
        groupsInTestament = listOf(
            BookGroupPresentationModel(
                group = BookGroup.Pentateuch,
                books = pentateuch,
            ),
            BookGroupPresentationModel(
                group = BookGroup.HistoricalBooks,
                books = historical,
            ),
        ),
        filterOptions = booksFilterOptions,
        shouldShowTestamentToggle = true,
        isFilterMenuVisible = false,
        isSortMenuVisible = false,
        sortOrder = null,
        layoutFormat = BookLayoutFormat.List,
    )
}
