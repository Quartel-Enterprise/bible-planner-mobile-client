package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.ChapterVerseContentModel
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.dao.VerseDao

class GetChapterVerseContentsUseCase(
    private val getChapterId: GetChapterIdUseCase,
    private val verseDao: VerseDao,
) : GetChapterVerseContents {
    // Why: the heading stays untrimmed, the exact text the reader shows above the verse.
    override suspend fun invoke(chapter: ChapterRef): Map<Int, ChapterVerseContentModel> {
        val chapterId = getChapterId(
            bookId = chapter.bookId,
            chapterNumber = chapter.chapterNumber,
        ) ?: return emptyMap()
        return verseDao
            .getVersesWithTextsByChapterId(chapterId)
            .mapNotNull { verseWithTexts ->
                verseWithTexts.texts
                    .find { it.bibleVersionId == chapter.bibleVersionId }
                    ?.let { verseText ->
                        verseWithTexts.verse.number to ChapterVerseContentModel(
                            text = verseText.text.trim(),
                            heading = verseText.heading?.takeIf(String::isNotBlank),
                        )
                    }
            }.toMap()
    }
}
