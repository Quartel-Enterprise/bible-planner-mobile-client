package com.quare.bibleplanner.feature.unlockpremium.presentation.model

import com.quare.bibleplanner.feature.unlockpremium.domain.model.plan.SubscriptionPlanType

sealed interface UnlockPremiumUiEvent {
    data object OnBackClick : UnlockPremiumUiEvent

    data class OnPlanSelected(
        val planType: SubscriptionPlanType,
    ) : UnlockPremiumUiEvent

    data object OnStartPremiumJourney : UnlockPremiumUiEvent
}
