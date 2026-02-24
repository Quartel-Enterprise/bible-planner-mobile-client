package com.quare.bibleplanner.feature.bookdetails.presentation.model

sealed interface BookDetailsUiAction {
    data object NavigateBack : BookDetailsUiAction

    data class NavigateToRoute(
        val route: Any,
    ) : BookDetailsUiAction
}
