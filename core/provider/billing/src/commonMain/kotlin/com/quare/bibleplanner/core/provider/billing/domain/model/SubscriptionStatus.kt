package com.quare.bibleplanner.core.provider.billing.domain.model

import kotlinx.datetime.LocalDateTime

sealed interface SubscriptionStatus {
    data object Free : SubscriptionStatus

    data class Pro(
        val planType: ProPlanType,
        val purchaseDate: LocalDateTime?,
        val expirationDate: LocalDateTime?,
        val willRenew: Boolean,
        val store: PurchaseStore?,
    ) : SubscriptionStatus
}
