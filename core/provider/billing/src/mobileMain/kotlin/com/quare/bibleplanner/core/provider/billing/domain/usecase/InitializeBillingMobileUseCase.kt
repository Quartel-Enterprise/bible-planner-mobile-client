package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.revenuecat.purchases.kmp.Purchases

class InitializeBillingMobileUseCase : InitializeBillingUseCase {
    override fun invoke() {
        Purchases.sharedInstance.getCustomerInfo(
            onSuccess = {},
            onError = {},
        )
    }
}
