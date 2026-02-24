package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

class IsWholeChapterReadUseCase(
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
) {
    suspend operator fun invoke(
        chapterNumber: Int,
        bookId: BookId,
    ): Boolean = chapterDao
        .getChapterByBookIdAndNumber(
            bookId = bookId.name,
            chapterNumber = chapterNumber,
        )?.let { chapter ->
            invoke(chapter.id)
        } ?: false

    suspend operator fun invoke(chapterId: Long): Boolean {
        val verses = verseDao.getVersesByChapterId(chapterId)
        return verses.isNotEmpty() && verses.all { it.isRead }
    }
}
