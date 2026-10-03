package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterRef

fun interface SaveVerseNote {
    suspend operator fun invoke(
        noteId: String?,
        chapter: ChapterRef,
        verseNumbers: List<Int>,
        text: String,
    )
}
