package com.quare.bibleplanner.feature.versenote.presentation.model

import com.quare.bibleplanner.core.model.book.BookId

data class VerseNoteUiState(
    val bookId: BookId,
    val chapterNumber: Int,
    val verseNumbers: List<Int>,
    val quote: String,
    val text: String,
    val isSaveEnabled: Boolean,
)
