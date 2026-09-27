package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId

fun interface GetVerseReference {
    suspend operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        verseNumbers: List<Int>,
    ): String
}
