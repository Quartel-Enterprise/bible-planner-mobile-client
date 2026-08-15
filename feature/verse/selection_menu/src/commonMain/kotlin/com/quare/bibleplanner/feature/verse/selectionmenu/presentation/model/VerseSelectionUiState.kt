package com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor

data class VerseSelectionUiState(
    val bookId: BookId,
    val chapterNumber: Int,
    val verseNumbers: List<Int>,
    val customColors: List<HighlightColor.Custom>,
    val activeColor: HighlightColor?,
    val isSelectionSaved: Boolean,
    val noteId: String?,
    val customColorPicker: CustomColorUiModel?,
)
