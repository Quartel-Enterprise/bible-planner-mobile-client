package com.quare.bibleplanner.core.provider.billing.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException

internal fun Throwable.toBillingException(): BillingException = when (this) {
    is PurchasesException -> when (code) {
        PurchasesErrorCode.PurchaseCancelledError -> BillingException.UserCancelled()
        PurchasesErrorCode.NetworkError -> BillingException.NetworkError()
        PurchasesErrorCode.PaymentPendingError -> BillingException.PaymentPending()
        else -> BillingException.Unknown(message)
    }

    else -> BillingException.Unknown(message)
}
