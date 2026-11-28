package com.quare.bibleplanner.feature.read.presentation.deletecolor

sealed interface DeleteHighlightColorUiAction {
    data class NotifyDeletion(
        val hasKeptHighlights: Boolean,
    ) : DeleteHighlightColorUiAction
}
