package com.quare.bibleplanner.feature.more.presentation.model

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

internal sealed interface MoreUiAction {
    data class OpenLink(
        val url: String,
    ) : MoreUiAction

    data object ShowNoProgressToDelete : MoreUiAction

    data class ShowSnackbar(
        val message: StringResource,
    ) : MoreUiAction

    data class Copy(
        val text: String,
    ) : MoreUiAction

    data class GoToRoute(
        val route: NavKey,
    ) : MoreUiAction
}
