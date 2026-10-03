package com.quare.bibleplanner.core.verseannotations.domain.model

import com.quare.bibleplanner.core.model.book.ChapterRef

data class VerseSelection(
    val chapter: ChapterRef,
    val verseNumbers: List<Int>,
) {
    val refs: List<VerseRef>
        get() = verseNumbers.map { verseNumber ->
            VerseRef(
                chapter = chapter,
                verseNumber = verseNumber,
            )
        }
}
