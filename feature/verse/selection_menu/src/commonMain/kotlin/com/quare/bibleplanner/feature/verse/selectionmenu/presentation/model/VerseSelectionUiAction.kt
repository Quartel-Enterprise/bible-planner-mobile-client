package com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model

sealed interface VerseSelectionUiAction {
    data class CopyToClipboard(
        val text: String,
    ) : VerseSelectionUiAction

    data object NotifyCopied : VerseSelectionUiAction
}
