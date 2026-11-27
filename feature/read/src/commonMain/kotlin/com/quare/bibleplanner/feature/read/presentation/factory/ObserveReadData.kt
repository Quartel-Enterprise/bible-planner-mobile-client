package com.quare.bibleplanner.feature.read.presentation.factory

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import kotlinx.coroutines.flow.Flow

fun interface ObserveReadData {
    operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        isInitiallyRead: Boolean,
        isFromBookDetails: Boolean,
        prependedChapters: List<ReadNavigationSuggestionModel>,
        appendedChapters: List<ReadNavigationSuggestionModel>,
    ): Flow<ReadDataUiModel>
}
