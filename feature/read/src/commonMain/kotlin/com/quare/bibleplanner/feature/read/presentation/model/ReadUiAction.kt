package com.quare.bibleplanner.feature.read.presentation.model

import androidx.navigation3.runtime.NavKey

sealed interface ReadUiAction {
    data object NavigateBack : ReadUiAction

    data class NavigateToRoute(
        val route: NavKey,
        val replace: Boolean,
    ) : ReadUiAction
}
