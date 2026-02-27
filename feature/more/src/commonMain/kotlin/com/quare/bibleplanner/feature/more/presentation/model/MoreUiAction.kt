package com.quare.bibleplanner.feature.more.presentation.model

internal sealed interface MoreUiAction {
    data class OpenLink(
        val url: String,
    ) : MoreUiAction

    data object ShowNoProgressToDelete : MoreUiAction

    data class GoToRoute(
        val route: Any,
    ) : MoreUiAction
}
