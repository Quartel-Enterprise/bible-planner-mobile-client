package com.quare.bibleplanner.core.model

import androidx.navigation3.runtime.NavKey

sealed interface NavigationCommand {
    data class Navigate(
        val route: NavKey,
    ) : NavigationCommand

    data class NavigateReplacingTop(
        val route: NavKey,
    ) : NavigationCommand

    data object NavigateBack : NavigationCommand
}
