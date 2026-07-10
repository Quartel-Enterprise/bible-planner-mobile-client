package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.room.dao.BookDao
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao

class UpdateWholeBookReadStatusIfNeededUseCase(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val trackEvent: TrackEvent,
) {
    suspend operator fun invoke(bookId: BookId) {
        val bookIdName = bookId.name
        val updatedChaptersInBook = chapterDao.getChaptersByBookId(bookIdName)
        val allChaptersRead = updatedChaptersInBook.isNotEmpty() && updatedChaptersInBook.all { it.isRead }

        val currentBook = bookDao.getBookById(bookIdName)
        if (currentBook?.isRead != allChaptersRead) {
            bookDao.updateBookReadStatus(
                bookId = bookIdName,
                isRead = allChaptersRead,
            )
            if (allChaptersRead && currentBook?.isRead == false) {
                trackEvent(
                    name = AnalyticsEventNames.BOOK_COMPLETED,
                    params = mapOf(
                        AnalyticsParams.BOOK_ID to bookId.name.lowercase(),
                        AnalyticsParams.SOURCE to SOURCE_LAST_CHAPTER,
                    ),
                )
            }
        }
    }

    private companion object {
        const val SOURCE_LAST_CHAPTER = "last_chapter"
    }
}
