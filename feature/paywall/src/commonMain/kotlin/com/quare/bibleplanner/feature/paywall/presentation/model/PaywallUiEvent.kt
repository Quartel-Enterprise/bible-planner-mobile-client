package com.quare.bibleplanner.feature.paywall.presentation.model

import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType

sealed interface PaywallUiEvent {
    data object OnBackClick : PaywallUiEvent

    data class OnPlanSelected(
        val planType: SubscriptionPlanType,
    ) : PaywallUiEvent

    data object OnStartPremiumJourneyClick : PaywallUiEvent

    data object OnRestorePurchases : PaywallUiEvent
}
