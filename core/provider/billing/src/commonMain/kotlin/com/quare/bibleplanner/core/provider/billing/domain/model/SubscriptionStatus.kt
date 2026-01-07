package com.quare.bibleplanner.core.provider.billing.domain.model

import kotlinx.datetime.LocalDateTime

sealed interface SubscriptionStatus {
    data object Free : SubscriptionStatus

    data class Pro(
        val planName: String?,
        val purchaseDate: LocalDateTime?,
        val expirationDate: LocalDateTime?,
        val willRenew: Boolean = true,
    ) : SubscriptionStatus
}
