package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

class UpdateSpecificRangeChapterReadStatusUseCase(
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val updateWholeBookReadStatusIfNeeded: UpdateWholeBookReadStatusIfNeededUseCase,
) {
    suspend operator fun invoke(
        chapterNumber: Int,
        startVerse: Int,
        endVerse: Int,
        isRead: Boolean,
        bookId: BookId,
    ) {
        val chapterEntity = chapterDao.getChapterByBookIdAndNumber(
            bookId = bookId.name,
            chapterNumber = chapterNumber,
        ) ?: return
        verseDao.updateVerseReadStatusRange(
            chapterId = chapterEntity.id,
            startVerse = startVerse,
            endVerse = endVerse,
            isRead = isRead,
        )

        // Check if Chapter status needs update after modifying verses
        val verses = verseDao.getVersesByChapterId(chapterEntity.id)
        val isChapterFullyRead = verses.isNotEmpty() && verses.all { it.isRead }

        if (chapterEntity.isRead != isChapterFullyRead) {
            chapterDao.updateChapterReadStatus(
                chapterId = chapterEntity.id,
                isRead = isChapterFullyRead,
            )
        }
        updateWholeBookReadStatusIfNeeded(bookId)
    }
}
