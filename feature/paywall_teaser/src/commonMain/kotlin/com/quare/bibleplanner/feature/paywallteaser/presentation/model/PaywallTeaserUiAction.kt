package com.quare.bibleplanner.feature.paywallteaser.presentation.model

internal sealed interface PaywallTeaserUiAction {
    data object NavigateBack : PaywallTeaserUiAction

    data object NavigateToPaywall : PaywallTeaserUiAction
}
