package com.quare.bibleplanner.feature.read.presentation.model

sealed interface ReadUiAction {
    data object NavigateBack : ReadUiAction

    data class NavigateToRoute(
        val route: Any,
        val replace: Boolean,
    ) : ReadUiAction
}
