package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterRef

fun interface SaveVerseNote {
    // Why: an empty text deletes the note so clearing and saving never leaves an empty card.
    suspend operator fun invoke(
        noteId: String?,
        chapter: ChapterRef,
        verseNumbers: List<Int>,
        text: String,
    )
}
