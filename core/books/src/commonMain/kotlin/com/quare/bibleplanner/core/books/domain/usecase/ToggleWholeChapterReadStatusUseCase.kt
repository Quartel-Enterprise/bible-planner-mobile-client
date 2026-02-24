package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId

class ToggleWholeChapterReadStatusUseCase(
    private val isWholeChapterRead: IsWholeChapterReadUseCase,
    private val updateWholeChapterRead: UpdateWholeChapterReadStatusUseCase,
) {
    suspend operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
    ) {
        val newStatus = !isWholeChapterRead(chapterNumber, bookId)
        updateWholeChapterRead(
            chapterNumber = chapterNumber,
            isRead = newStatus,
            bookId = bookId,
        )
    }
}
