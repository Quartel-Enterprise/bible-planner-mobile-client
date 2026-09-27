package com.quare.bibleplanner.feature.verse.annotations.presentation.model

internal data class AnnotationsOverlayState(
    val openMenuItemKey: String?,
    val openFilterMenu: AnnotationFilterMenu?,
    val pendingRemoval: AnnotationItemUiModel?,
    val isCustomRangePickerOpen: Boolean,
    val isSearchOpen: Boolean,
)
