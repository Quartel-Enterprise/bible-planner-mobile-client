package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId

class GetNewTestamentIdsUseCase {
    operator fun invoke(): List<BookId> = BookId.entries.filter { it.ordinal >= BookId.MAT.ordinal }
}
