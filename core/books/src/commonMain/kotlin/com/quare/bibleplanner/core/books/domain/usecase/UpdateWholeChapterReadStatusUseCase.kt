package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class UpdateWholeChapterReadStatusUseCase(
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val updateWholeBookReadStatusIfNeeded: UpdateWholeBookReadStatusIfNeededUseCase,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {
    suspend operator fun invoke(
        chapterNumber: Int,
        isRead: Boolean,
        bookId: BookId,
    ) {
        val chapterEntity = chapterDao.getChapterByBookIdAndNumber(
            bookId = bookId.name,
            chapterNumber = chapterNumber,
        )
        val chapterId = chapterEntity?.id ?: return
        val updatedAt = currentTimestampProvider.getCurrentTimestamp()
        coroutineScope {
            val updateChapterReadStatusDeferred = async {
                chapterDao.updateChapterReadStatus(
                    chapterId = chapterId,
                    isRead = isRead,
                    updatedAt = updatedAt,
                )
            }
            val updateVersesReadStatusByChapterDeferred = async {
                verseDao.updateVersesReadStatusByChapter(
                    chapterId = chapterId,
                    isRead = isRead,
                )
            }
            updateChapterReadStatusDeferred.await()
            updateVersesReadStatusByChapterDeferred.await()
            updateWholeBookReadStatusIfNeeded(bookId)
        }
    }
}
