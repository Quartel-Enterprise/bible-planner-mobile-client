package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterRef

fun interface SaveVerseNote {
    /**
     * Writes the note, creating it when [noteId] is null. An empty [text] deletes it instead, so
     * clearing a note and saving does not leave an empty card in the chapter.
     */
    suspend operator fun invoke(
        noteId: String?,
        chapter: ChapterRef,
        verseNumbers: List<Int>,
        text: String,
    )
}
