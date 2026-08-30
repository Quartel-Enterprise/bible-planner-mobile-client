package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel

fun interface GetPreviousChapter {
    /** The chapter that comes before in the active reading order, or null at the start of it. */
    suspend operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        shouldForceCanonOrder: Boolean,
    ): ReadNavigationSuggestionModel?
}
