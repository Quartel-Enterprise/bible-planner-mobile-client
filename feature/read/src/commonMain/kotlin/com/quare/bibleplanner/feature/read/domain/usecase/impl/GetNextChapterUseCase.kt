package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetNextChapter
import com.quare.bibleplanner.feature.read.domain.usecase.GetReadNavigationSuggestionsModelUseCase
import kotlinx.coroutines.flow.first

internal class GetNextChapterUseCase(
    private val getReadNavigationSuggestionsModel: GetReadNavigationSuggestionsModelUseCase,
) : GetNextChapter {
    override suspend fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        shouldForceCanonOrder: Boolean,
    ): ReadNavigationSuggestionModel? = getReadNavigationSuggestionsModel(
        shouldForceCanonOrder = shouldForceCanonOrder,
        currentBookId = bookId,
        currentChapterNumber = chapterNumber,
    ).first().next
}
