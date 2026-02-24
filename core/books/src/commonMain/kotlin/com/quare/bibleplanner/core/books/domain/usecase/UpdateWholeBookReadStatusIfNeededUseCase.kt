package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao

class UpdateWholeBookReadStatusIfNeededUseCase(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
) {
    suspend operator fun invoke(bookId: BookId) {
        val bookIdName = bookId.name
        val updatedChaptersInBook = chapterDao.getChaptersByBookId(bookIdName)
        val allChaptersRead = updatedChaptersInBook.all { it.isRead }

        val currentBook = bookDao.getBookById(bookIdName)
        if (currentBook?.isRead != allChaptersRead) {
            bookDao.updateBookReadStatus(
                bookId = bookIdName,
                isRead = allChaptersRead,
            )
        }
    }
}
