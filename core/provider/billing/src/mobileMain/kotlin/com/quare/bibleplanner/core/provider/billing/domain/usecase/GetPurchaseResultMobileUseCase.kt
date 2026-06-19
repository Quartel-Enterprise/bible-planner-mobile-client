package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.mapper.toBillingException
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.result.awaitPurchaseResult

class GetPurchaseResultMobileUseCase(
    private val purchases: Purchases,
) : GetPurchaseResultUseCase {
    override suspend fun invoke(storePackage: StorePackage): Result<Unit> =
        (storePackage.originalObject as? Package)?.let { safePackage ->
            purchases
                .awaitPurchaseResult(safePackage)
                .map {}
                .fold(
                    onSuccess = { Result.success(Unit) },
                    onFailure = { error -> Result.failure(error.toBillingException()) },
                )
        } ?: Result.failure(Exception("Invalid package object"))
}
