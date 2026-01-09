package com.quare.bibleplanner.core.provider.billing.domain.model

sealed class BillingException : Exception() {
    class UserCancelled : BillingException()

    class NetworkError : BillingException()

    class PaymentPending : BillingException()

    class RestorePurchaseFailed : BillingException()

    data class Unknown(
        override val message: String? = null,
    ) : BillingException()
}
