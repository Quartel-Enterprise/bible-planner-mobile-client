package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.book.BookId

/** What the user is doing right now, as opposed to what the chapter itself holds. */
data class ReadInteractionUiModel(
    val selectedBookId: BookId?,
    val selectedChapterNumber: Int?,
    val selectedVerseNumbers: Set<Int>,
    val customColorPicker: CustomColorUiModel?,
    val isSidePanelOpen: Boolean,
)
