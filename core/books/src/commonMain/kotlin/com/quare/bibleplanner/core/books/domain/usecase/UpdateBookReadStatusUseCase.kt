package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class UpdateBookReadStatusUseCase(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke(
        bookId: BookId,
        isRead: Boolean,
    ) {
        val bookIdName = bookId.name
        val updatedAt = currentTimestampProvider.getCurrentTimestamp()
        coroutineScope {
            launch {
                bookDao.updateBookReadStatus(
                    bookId = bookIdName,
                    isRead = isRead,
                )
            }
            launch {
                chapterDao.updateChaptersReadStatusByBook(
                    bookId = bookIdName,
                    isRead = isRead,
                    updatedAt = updatedAt,
                )
            }
            launch {
                verseDao.updateVersesReadStatusByBook(
                    bookId = bookIdName,
                    isRead = isRead,
                )
            }
        }
    }
}
