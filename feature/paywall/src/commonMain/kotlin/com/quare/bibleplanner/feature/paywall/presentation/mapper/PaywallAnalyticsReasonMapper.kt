package com.quare.bibleplanner.feature.paywall.presentation.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException

class PaywallAnalyticsReasonMapper {
    fun map(throwable: Throwable): String = when (throwable) {
        is BillingException.UserCancelled -> USER_CANCELLED
        is BillingException.NetworkError -> NETWORK
        is BillingException.PaymentPending -> PAYMENT_PENDING
        is BillingException.RestorePurchaseFailed -> RESTORE_FAILED
        else -> UNKNOWN
    }

    private companion object {
        const val USER_CANCELLED = "user_cancelled"
        const val NETWORK = "network"
        const val PAYMENT_PENDING = "payment_pending"
        const val RESTORE_FAILED = "restore_failed"
        const val UNKNOWN = "unknown"
    }
}
