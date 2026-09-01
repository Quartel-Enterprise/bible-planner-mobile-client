package com.quare.bibleplanner.feature.paywall.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed interface PaywallUiAction {
    data class ShowSnackbar(
        val message: StringResource,
        val args: List<Any> = emptyList(),
    ) : PaywallUiAction
}
