package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import org.jetbrains.compose.resources.StringResource

/**
 * The selection panel's state. It exists only while at least one verse is selected, which is what
 * shows the panel. The chapter is part of it because vertical reading puts two chapters on screen
 * and a selection never spans both.
 */
data class ReadSelectionUiModel(
    val bookId: BookId,
    val bookStringResource: StringResource,
    val chapterNumber: Int,
    val verseNumbers: List<Int>,
    val customColors: List<HighlightColor.Custom>,
    val activeColor: HighlightColor?,
    val isSelectionSaved: Boolean,
    val noteId: String?,
    val customColorPicker: CustomColorUiModel?,
)
