package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.model.book.BookId

fun interface GetVersesShareContent {
    /** Null when the selected version has no text for the passage. */
    suspend operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        verseNumbers: List<Int>,
    ): VersesShareContentModel?
}
