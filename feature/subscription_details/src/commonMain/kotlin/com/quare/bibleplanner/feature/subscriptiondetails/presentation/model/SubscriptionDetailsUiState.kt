package com.quare.bibleplanner.feature.subscriptiondetails.presentation.model

import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import kotlinx.datetime.LocalDateTime

sealed interface SubscriptionDetailsUiState {
    data object Loading : SubscriptionDetailsUiState

    data class Loaded(
        val planType: ProPlanType,
        val purchaseDate: LocalDateTime?,
        val expirationDate: LocalDateTime?,
        val willRenew: Boolean = true,
    ) : SubscriptionDetailsUiState

    data object Error : SubscriptionDetailsUiState // Fallback if somehow not pro
}
