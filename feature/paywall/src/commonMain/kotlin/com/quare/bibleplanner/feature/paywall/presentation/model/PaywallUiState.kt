package com.quare.bibleplanner.feature.paywall.presentation.model

sealed interface PaywallUiState {
    data class Success(
        val subscriptionPlans: List<SubscriptionPlanPresentationModel>,
        val isPurchasing: Boolean,
    ) : PaywallUiState

    data object Error : PaywallUiState

    data object Loading : PaywallUiState
}
