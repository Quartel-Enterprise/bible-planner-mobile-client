package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

class GetChapterVerseTextsUseCase(
    private val getChapterId: GetChapterIdUseCase,
    private val verseDao: VerseDao,
) : GetChapterVerseTexts {
    override suspend fun invoke(chapter: ChapterRef): Map<Int, String> {
        val chapterId = getChapterId(
            bookId = chapter.bookId,
            chapterNumber = chapter.chapterNumber,
        ) ?: return emptyMap()
        return verseDao
            .getVersesWithTextsByChapterId(chapterId)
            .mapNotNull { verseWithTexts ->
                verseWithTexts.texts
                    .find { it.bibleVersionId == chapter.bibleVersionId }
                    ?.let { verseText -> verseWithTexts.verse.number to verseText.text.trim() }
            }.toMap()
    }
}
