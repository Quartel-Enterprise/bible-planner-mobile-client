package com.quare.bibleplanner.feature.subscriptiondetails.presentation.model

import kotlinx.datetime.LocalDateTime

sealed interface SubscriptionDetailsUiState {
    data object Loading : SubscriptionDetailsUiState

    data class Loaded(
        val planName: String?,
        val purchaseDate: LocalDateTime?,
        val expirationDate: LocalDateTime?,
        val willRenew: Boolean = true,
    ) : SubscriptionDetailsUiState

    data object Error : SubscriptionDetailsUiState // Fallback if somehow not pro
}
