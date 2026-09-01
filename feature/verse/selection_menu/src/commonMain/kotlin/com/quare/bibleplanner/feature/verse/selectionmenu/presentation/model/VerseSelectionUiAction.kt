package com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed interface VerseSelectionUiAction {
    data class CopyToClipboard(
        val text: String,
    ) : VerseSelectionUiAction

    data class ShowMessage(
        val stringResource: StringResource,
    ) : VerseSelectionUiAction
}
