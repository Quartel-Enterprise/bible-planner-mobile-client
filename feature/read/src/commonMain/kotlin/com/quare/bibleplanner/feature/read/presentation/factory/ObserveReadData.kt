package com.quare.bibleplanner.feature.read.presentation.factory

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource

fun interface ObserveReadData {
    /**
     * [appendedChapters] are the chapters vertical reading has already pulled in behind this one, in
     * reading order. The reader grows that list as it reaches the end, so the chain has no limit.
     */
    operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        bookStringResource: StringResource,
        isInitiallyRead: Boolean,
        isFromBookDetails: Boolean,
        appendedChapters: List<ReadNavigationSuggestionModel>,
    ): Flow<ReadDataUiModel>
}
