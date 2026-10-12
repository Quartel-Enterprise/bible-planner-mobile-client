package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

class GetChapterVerseHeadingsUseCase(
    private val getChapterId: GetChapterIdUseCase,
    private val verseDao: VerseDao,
) : GetChapterVerseHeadings {
    // Why: untrimmed, so it stays the exact text the reader shows above the verse.
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
                    ?.heading
                    ?.takeIf(String::isNotBlank)
                    ?.let { heading -> verseWithTexts.verse.number to heading }
            }.toMap()
    }
}
