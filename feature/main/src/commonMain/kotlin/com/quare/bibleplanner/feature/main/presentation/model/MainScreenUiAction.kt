package com.quare.bibleplanner.feature.main.presentation.model

sealed interface MainScreenUiAction {
    data object ClearFabs : MainScreenUiAction

    data class NavigateToBottomRoute(
        val route: Any,
    ) : MainScreenUiAction
}
