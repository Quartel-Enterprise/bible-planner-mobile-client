package com.quare.bibleplanner.feature.read.fixture

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel

internal fun readChapter(
    chapterNumber: Int,
    verseCount: Int,
): ReadChapterUiModel = ReadChapterUiModel(
    chapter = ChapterRef(
        bibleVersionId = "WEB",
        bookId = BookId.GEN,
        chapterNumber = chapterNumber,
    ),
    isRead = false,
    verses = (1..verseCount).map { number ->
        VerseUiModel(
            number = number,
            heading = null,
            text = "Verse $number",
            isSelected = false,
            highlightColor = null,
            isSaved = false,
            noteMark = null,
        )
    },
)
