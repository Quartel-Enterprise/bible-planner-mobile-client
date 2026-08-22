package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId

fun interface IsWholeChapterRead {
    suspend operator fun invoke(
        chapterNumber: Int,
        bookId: BookId,
    ): Boolean
}
