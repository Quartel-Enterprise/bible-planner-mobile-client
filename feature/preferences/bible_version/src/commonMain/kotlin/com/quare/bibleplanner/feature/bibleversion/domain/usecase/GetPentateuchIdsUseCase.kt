package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId

class GetPentateuchIdsUseCase {
    operator fun invoke(): List<BookId> = listOf(BookId.GEN, BookId.EXO, BookId.LEV, BookId.NUM, BookId.DEU)
}
