package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId

class GetPrioritizedBookIdsUseCase(
    private val getPentateuchIds: GetPentateuchIdsUseCase,
    private val getNewTestamentIds: GetNewTestamentIdsUseCase,
) {
    operator fun invoke(): List<BookId> {
        val pentateuch = getPentateuchIds()
        val newTestament = getNewTestamentIds()
        val rest = BookId.entries.filter { it !in pentateuch && it !in newTestament }
        return pentateuch + newTestament + rest
    }
}
