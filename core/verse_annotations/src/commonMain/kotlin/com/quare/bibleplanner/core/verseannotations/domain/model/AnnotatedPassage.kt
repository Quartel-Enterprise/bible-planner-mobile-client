package com.quare.bibleplanner.core.verseannotations.domain.model

import com.quare.bibleplanner.core.model.book.ChapterRef

data class AnnotatedPassage(
    val chapter: ChapterRef,
    val verseNumbers: List<Int>,
    val highlightColor: HighlightColor?,
    val isSaved: Boolean,
    val note: VerseNote?,
    val updatedAtEpochMillis: Long,
) {
    val refs: List<VerseRef>
        get() = verseNumbers.map { verseNumber ->
            VerseRef(
                chapter = chapter,
                verseNumber = verseNumber,
            )
        }
}
