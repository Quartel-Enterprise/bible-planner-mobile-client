package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.util.getVerseReferenceLabel
import com.quare.bibleplanner.core.model.book.BookId

class GetVerseReferenceUseCase : GetVerseReference {
    override suspend fun invoke(
        bookId: BookId,
        chapterNumber: Int,
        verseNumbers: List<Int>,
    ): String = getVerseReferenceLabel(
        bookId = bookId,
        chapterNumber = chapterNumber,
        verseNumbers = verseNumbers,
    )
}
