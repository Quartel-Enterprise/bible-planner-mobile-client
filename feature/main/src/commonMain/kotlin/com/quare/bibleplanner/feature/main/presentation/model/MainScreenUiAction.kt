package com.quare.bibleplanner.feature.main.presentation.model

import androidx.navigation3.runtime.NavKey

sealed interface MainScreenUiAction {
    data class NavigateToBottomRoute(
        val route: NavKey,
    ) : MainScreenUiAction
}
