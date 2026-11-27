package com.quare.bibleplanner.feature.paywall.presentation.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallError

class PaywallExceptionMapper {
    fun map(throwable: Throwable): PaywallError = when (throwable) {
        is BillingException.UserCancelled -> PaywallError.PURCHASE_CANCELLED
        is BillingException.NetworkError -> PaywallError.NETWORK
        is BillingException.PaymentPending -> PaywallError.PAYMENT_PENDING
        is BillingException.RestorePurchaseFailed -> PaywallError.RESTORE_PURCHASE_FAILED
        is BillingException.BrowserCheckoutNotConfirmed -> PaywallError.BROWSER_CHECKOUT_NOT_CONFIRMED
        else -> PaywallError.UNKNOWN
    }
}
