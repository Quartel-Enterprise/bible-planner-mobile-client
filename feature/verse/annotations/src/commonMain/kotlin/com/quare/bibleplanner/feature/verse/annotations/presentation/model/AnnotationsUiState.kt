package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable

internal data class AnnotationsUiState(
    val content: Loadable<AnnotationsContentUiModel>,
    val openMenuItemKey: String?,
    val openFilterMenu: AnnotationFilterMenu?,
    val pendingRemoval: AnnotationItemUiModel?,
    val isCustomRangePickerOpen: Boolean,
    val isSearchOpen: Boolean,
    val searchQuery: String,
)
