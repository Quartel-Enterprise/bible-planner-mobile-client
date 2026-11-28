package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor

data class ColorFilterUiModel(
    val color: HighlightColor,
    val count: Int,
    val isSelected: Boolean,
)
