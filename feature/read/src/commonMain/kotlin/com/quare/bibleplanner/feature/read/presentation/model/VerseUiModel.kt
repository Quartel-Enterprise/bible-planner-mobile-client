package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor

data class VerseUiModel(
    val number: Int,
    val heading: String?,
    val text: String,
    val isSelected: Boolean,
    val highlightColor: HighlightColor?,
    val isSaved: Boolean,
    val noteId: String?,
)
