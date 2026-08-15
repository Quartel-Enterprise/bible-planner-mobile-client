package com.quare.bibleplanner.core.verseannotations.domain.model

import com.quare.bibleplanner.core.model.book.BookId

data class VerseNote(
    val id: String,
    val bookId: BookId,
    val chapterNumber: Int,
    val verseNumbers: List<Int>,
    val text: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
