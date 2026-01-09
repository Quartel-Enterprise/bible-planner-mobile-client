package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.result.awaitPurchaseResult

class GetPurchaseResultMobileUseCase : GetPurchaseResultUseCase {
    override suspend fun invoke(storePackage: StorePackage): Result<Unit> =
        (storePackage.originalObject as? Package)?.let { safePackage ->
            Purchases.sharedInstance
                .awaitPurchaseResult(safePackage)
                .map {}
                .fold(
                    onSuccess = { Result.success(Unit) },
                    onFailure = { error ->
                        val billingException = when (error) {
                            is PurchasesException -> {
                                when (error.code) {
                                    PurchasesErrorCode.PurchaseCancelledError -> BillingException.UserCancelled()
                                    PurchasesErrorCode.NetworkError -> BillingException.NetworkError()
                                    PurchasesErrorCode.PaymentPendingError -> BillingException.PaymentPending()
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
        } ?: Result.failure(Exception("Invalid package object"))
}
