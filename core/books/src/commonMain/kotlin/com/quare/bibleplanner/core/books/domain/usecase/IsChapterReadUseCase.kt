package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

class IsChapterReadUseCase(
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val isWholeChapterRead: IsWholeChapterReadUseCase,
) {
    suspend operator fun invoke(chapterModel: ChapterModel): Boolean {
        val chapter = chapterDao.getChapterByBookIdAndNumber(
            bookId = chapterModel.bookId.name,
            chapterNumber = chapterModel.number,
        ) ?: return false

        val startVerse = chapterModel.startVerse
        val endVerse = chapterModel.endVerse
        val chapterId = chapter.id

        return when {
            startVerse != null && endVerse != null -> {
                val verses = verseDao.getVersesByChapterId(chapterId)
                val requiredVerses = startVerse..endVerse
                requiredVerses.all { verseNumber ->
                    verses.any { it.number == verseNumber && it.isRead }
                }
            }

            startVerse != null -> {
                val verses = verseDao.getVersesByChapterId(chapterId)
                verses
                    .filter { it.number >= startVerse }
                    .all { it.isRead }
            }

            else -> {
                if (chapter.isRead) {
                    true
                } else {
                    isWholeChapterRead(chapterId)
                }
            }
        }
    }
}
