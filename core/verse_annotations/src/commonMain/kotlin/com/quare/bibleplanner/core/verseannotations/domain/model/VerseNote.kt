package com.quare.bibleplanner.core.verseannotations.domain.model

import com.quare.bibleplanner.core.model.book.ChapterRef

data class VerseNote(
    val id: String,
    val chapter: ChapterRef,
    val verseNumbers: List<Int>,
    val text: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
