package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.mapper.toProEntitlement
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import com.revenuecat.purchases.kmp.result.awaitCustomerInfoResult

internal class IsProUserInRevenueCatUseCase {
    suspend operator fun invoke(): Boolean = Purchases.sharedInstance
        .awaitCustomerInfoResult(CacheFetchPolicy.NOT_STALE_CACHED_OR_CURRENT)
        .getOrNull()
        ?.toProEntitlement() != null
}
