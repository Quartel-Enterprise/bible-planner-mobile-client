package com.quare.bibleplanner.feature.bookdetails.fixture

import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiState
import com.quare.bibleplanner.feature.bookdetails.presentation.utils.toSynopsisResource

/**
 * Psalms, half read, with the synopsis open. The book is picked for its 150 chapters: on the
 * landscape shot the grid fills its whole column, where a short book leaves the bottom half of the
 * tablet empty.
 */
private const val BOOK_CHAPTERS = 150
private const val READ_CHAPTERS = 76

internal fun bookDetailsUiState(bookCategoryName: String): BookDetailsUiState.Success = BookDetailsUiState.Success(
    id = BookId.PSA,
    nameStringResource = BookId.PSA.toBookNameResource(),
    synopsisStringResource = BookId.PSA.toSynopsisResource(),
    chapters = (1..BOOK_CHAPTERS).map { number ->
        BookChapterModel(
            number = number,
            verses = emptyList(),
            isRead = number <= READ_CHAPTERS,
            readUpdatedAt = null,
        )
    },
    progress = READ_CHAPTERS.toFloat() / BOOK_CHAPTERS,
    readChaptersCount = READ_CHAPTERS,
    totalChaptersCount = BOOK_CHAPTERS,
    areAllChaptersRead = false,
    isFavorite = true,
    bookGroup = BookGroup.WisdomBooks,
    bookCategoryName = bookCategoryName,
    isSynopsisExpanded = true,
)
