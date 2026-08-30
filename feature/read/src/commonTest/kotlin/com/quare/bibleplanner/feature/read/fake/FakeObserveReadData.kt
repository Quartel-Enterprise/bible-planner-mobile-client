package com.quare.bibleplanner.feature.read.fake

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.presentation.factory.ObserveReadData
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.StringResource

internal class FakeObserveReadData(
    val data: MutableStateFlow<ReadDataUiModel>,
) : ObserveReadData {
    override fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        bookStringResource: StringResource,
        isInitiallyRead: Boolean,
        isFromBookDetails: Boolean,
        prependedChapters: List<ReadNavigationSuggestionModel>,
        appendedChapters: List<ReadNavigationSuggestionModel>,
    ): Flow<ReadDataUiModel> = data
}
