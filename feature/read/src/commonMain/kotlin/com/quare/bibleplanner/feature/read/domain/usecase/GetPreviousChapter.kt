package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel

fun interface GetPreviousChapter {
    suspend operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        shouldForceCanonOrder: Boolean,
    ): ReadNavigationSuggestionModel?
}
