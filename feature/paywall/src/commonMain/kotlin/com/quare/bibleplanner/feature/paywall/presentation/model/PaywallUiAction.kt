package com.quare.bibleplanner.feature.paywall.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed interface PaywallUiAction {
    data object NavigateBack : PaywallUiAction

    data class NavigateTo(
        val route: Any,
    ) : PaywallUiAction

    data class ShowSnackbar(
        val message: StringResource,
        val args: List<Any> = emptyList(),
    ) : PaywallUiAction
}
