package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.revenuecat.purchases.kmp.Purchases

class InitializeBillingMobileUseCase(
    private val purchases: Purchases,
) : InitializeBillingUseCase {
    override fun invoke() {
        purchases.getCustomerInfo(
            onSuccess = {},
            onError = {},
        )
    }
}
