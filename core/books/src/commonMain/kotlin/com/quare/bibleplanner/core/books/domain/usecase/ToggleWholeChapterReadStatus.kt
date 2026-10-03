package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId

fun interface ToggleWholeChapterReadStatus {
    suspend operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
    ): Boolean
}
