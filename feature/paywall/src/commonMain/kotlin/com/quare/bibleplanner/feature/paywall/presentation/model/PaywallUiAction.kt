package com.quare.bibleplanner.feature.paywall.presentation.model

sealed interface PaywallUiAction {
    data class ShowError(
        val error: PaywallError,
        val storeName: String,
    ) : PaywallUiAction
}
