package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
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
    private val trackEvent: TrackEvent,
) {
    suspend operator fun invoke(
        bookId: BookId,
        isRead: Boolean,
    ) {
        val bookIdName = bookId.name
        val wasBookComplete = chapterDao.getChaptersByBookId(bookIdName).let { chapters ->
            chapters.isNotEmpty() && chapters.all { it.isRead }
        }
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
        if (isRead && !wasBookComplete) {
            trackEvent(
                name = AnalyticsEventNames.BOOK_COMPLETED,
                params = mapOf(
                    AnalyticsParams.BOOK_ID to bookId.name.lowercase(),
                    AnalyticsParams.SOURCE to SOURCE_TOGGLE_ALL,
                ),
            )
        }
    }

    private companion object {
        const val SOURCE_TOGGLE_ALL = "toggle_all"
    }
}
