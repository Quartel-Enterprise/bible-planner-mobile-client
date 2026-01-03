package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.result.awaitCustomerInfoResult

internal class IsPremiumUserMobileUseCase : IsPremiumUserUseCase {
    override suspend fun invoke(): Boolean = Purchases.sharedInstance
        .awaitCustomerInfoResult(CacheFetchPolicy.NOT_STALE_CACHED_OR_CURRENT)
        .getOrNull()
        ?.toProEntitlement() != null

    private fun CustomerInfo.toProEntitlement(): EntitlementInfo? = entitlements.active["Bible Planner Pro"]
}
