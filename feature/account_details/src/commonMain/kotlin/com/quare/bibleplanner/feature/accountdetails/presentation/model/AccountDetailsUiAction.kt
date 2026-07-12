package com.quare.bibleplanner.feature.accountdetails.presentation.model

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

internal sealed interface AccountDetailsUiAction {
    data class NavigateToRoute(
        val route: NavKey,
    ) : AccountDetailsUiAction

    data class ReplaceWithRoute(
        val route: NavKey,
    ) : AccountDetailsUiAction

    data class ShowSnackbar(
        val message: StringResource,
    ) : AccountDetailsUiAction
}
