package com.quare.bibleplanner.core.provider.billing.domain.model

sealed class BillingException : Exception() {
    data object UserCancelled : BillingException()

    data object NetworkError : BillingException()

    data object PaymentPending : BillingException()

    data class Unknown(
        override val message: String? = null,
    ) : BillingException()
}
