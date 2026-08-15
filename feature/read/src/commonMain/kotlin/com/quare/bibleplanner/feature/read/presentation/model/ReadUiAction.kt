package com.quare.bibleplanner.feature.read.presentation.model

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

sealed interface ReadUiAction {
    data object NavigateBack : ReadUiAction

    data class NavigateToRoute(
        val route: NavKey,
        val replace: Boolean,
    ) : ReadUiAction

    data class CopyToClipboard(
        val text: String,
    ) : ReadUiAction

    data class ShowMessage(
        val stringResource: StringResource,
    ) : ReadUiAction
}
