package com.quare.bibleplanner.core.verseannotations.domain.model

import com.quare.bibleplanner.core.model.book.ChapterRef

// Why: a selection never spans two chapters or versions; vertical reading shows two
// chapters, and tapping the other one starts over.
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
