package com.quare.bibleplanner.feature.paywall.presentation.model

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource

sealed interface PaywallUiAction {
    data object NavigateBack : PaywallUiAction

    data class NavigateTo(
        val route: NavKey,
    ) : PaywallUiAction

    data class NavigateToLoginWarning(
        val reason: String,
    ) : PaywallUiAction

    data class ShowSnackbar(
        val message: StringResource,
        val args: List<Any> = emptyList(),
    ) : PaywallUiAction
}
