package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao

class GetChapterIdUseCase(
    private val chapterDao: ChapterDao,
) {
    suspend operator fun invoke(
        bookId: BookId,
        chapterNumber: Int,
    ): Long? = chapterDao
        .getChapterByBookIdAndNumber(
            bookId = bookId.name,
            chapterNumber = chapterNumber,
        )?.id
}
