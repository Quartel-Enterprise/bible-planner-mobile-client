package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel

fun interface GetNextChapter {
    /** The chapter that follows in the active reading order, or null at the end of it. */
    suspend operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        shouldForceCanonOrder: Boolean,
    ): ReadNavigationSuggestionModel?
}
