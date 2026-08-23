package com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

sealed interface VerseSelectionUiAction {
    data object NavigateBack : VerseSelectionUiAction

    data class NavigateToRoute(
        val route: NavKey,
    ) : VerseSelectionUiAction

    data class CopyToClipboard(
        val text: String,
    ) : VerseSelectionUiAction

    data class ShowMessage(
        val stringResource: StringResource,
    ) : VerseSelectionUiAction
}
