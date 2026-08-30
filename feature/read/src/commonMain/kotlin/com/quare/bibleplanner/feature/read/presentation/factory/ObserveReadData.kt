package com.quare.bibleplanner.feature.read.presentation.factory

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource

fun interface ObserveReadData {
    /**
     * [prependedChapters] and [appendedChapters] are the chapters vertical reading has already pulled
     * in before and behind this one, both in reading order. The reader grows either list as it
     * reaches that end, so the chain has no limit in either direction.
     */
    operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        bookStringResource: StringResource,
        isInitiallyRead: Boolean,
        isFromBookDetails: Boolean,
        prependedChapters: List<ReadNavigationSuggestionModel>,
        appendedChapters: List<ReadNavigationSuggestionModel>,
    ): Flow<ReadDataUiModel>
}
