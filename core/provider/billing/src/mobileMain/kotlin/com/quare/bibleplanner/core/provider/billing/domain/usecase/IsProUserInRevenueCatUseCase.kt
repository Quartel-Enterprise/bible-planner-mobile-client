package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.mapper.toProEntitlement
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import com.revenuecat.purchases.kmp.result.awaitCustomerInfoResult

internal class IsProUserInRevenueCatUseCase(
    private val purchases: Purchases,
) : IsProUserInRevenueCat {
    override suspend fun invoke(): Boolean = purchases
        .awaitCustomerInfoResult(CacheFetchPolicy.NOT_STALE_CACHED_OR_CURRENT)
        .getOrNull()
        ?.toProEntitlement() != null
}
