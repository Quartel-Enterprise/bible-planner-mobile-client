package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.result.awaitRestoreResult

internal class GetRestorePurchaseResultMobileUseCase : GetRestorePurchaseResultUseCase {
    override suspend fun invoke(): Result<Unit> = Purchases.sharedInstance
        .awaitRestoreResult()
        .map {}
        .fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { error ->
                val billingException = when (error) {
                    is PurchasesException -> {
                        when (error.code) {
                            PurchasesErrorCode.PurchaseCancelledError -> BillingException.UserCancelled
                            PurchasesErrorCode.NetworkError -> BillingException.NetworkError
                            PurchasesErrorCode.PaymentPendingError -> BillingException.PaymentPending
                            else -> BillingException.Unknown(error.message)
                        }
                    }

                    else -> {
                        BillingException.Unknown(error.message)
                    }
                }
                Result.failure(billingException)
            },
        )
}
