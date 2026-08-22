package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId

/** @return the chapter's read state after the toggle. */
fun interface ToggleWholeChapterReadStatus {
    suspend operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
    ): Boolean
}
