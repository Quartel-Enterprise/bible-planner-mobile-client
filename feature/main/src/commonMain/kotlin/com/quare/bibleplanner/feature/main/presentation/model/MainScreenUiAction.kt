package com.quare.bibleplanner.feature.main.presentation.model

sealed interface MainScreenUiAction {
    data object ClearScaffoldStates : MainScreenUiAction
    data class NavigateToBottomRoute(
        val route: Any,
    ) : MainScreenUiAction
}
