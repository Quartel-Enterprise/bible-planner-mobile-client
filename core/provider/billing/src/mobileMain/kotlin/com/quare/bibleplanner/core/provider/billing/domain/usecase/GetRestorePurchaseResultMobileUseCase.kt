package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.quare.bibleplanner.core.provider.billing.mapper.toBillingException
import com.quare.bibleplanner.core.provider.billing.mapper.toProEntitlement
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.result.awaitRestoreResult

internal class GetRestorePurchaseResultMobileUseCase(
    private val purchases: Purchases,
) : GetRestorePurchaseResultUseCase {
    override suspend fun invoke(): Result<Unit> = purchases
        .awaitRestoreResult()
        .fold(
            onSuccess = { customerInfo: CustomerInfo ->
                customerInfo.toProEntitlement()?.let {
                    Result.success(Unit)
                } ?: Result.failure(BillingException.RestorePurchaseFailed())
            },
            onFailure = { error -> Result.failure(error.toBillingException()) },
        )
}
