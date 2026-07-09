package com.quare.bibleplanner.feature.bookdetails.presentation.model

import androidx.navigation3.runtime.NavKey

sealed interface BookDetailsUiAction {
    data object NavigateBack : BookDetailsUiAction

    data class NavigateToRoute(
        val route: NavKey,
    ) : BookDetailsUiAction
}
