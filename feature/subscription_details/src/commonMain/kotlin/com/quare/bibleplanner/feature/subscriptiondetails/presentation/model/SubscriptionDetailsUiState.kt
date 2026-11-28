package com.quare.bibleplanner.feature.subscriptiondetails.presentation.model

import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore
import kotlinx.datetime.LocalDateTime

sealed interface SubscriptionDetailsUiState {
    data object Loading : SubscriptionDetailsUiState

    data class Loaded(
        val planType: ProPlanType,
        val purchaseDate: LocalDateTime?,
        val expirationDate: LocalDateTime?,
        val willRenew: Boolean,
        val store: PurchaseStore?,
    ) : SubscriptionDetailsUiState

    data object Error : SubscriptionDetailsUiState
}
