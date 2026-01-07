package com.quare.bibleplanner.core.provider.billing.domain.model

import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction

class SubscriptionPurchasesDelegate(
    private val onCustomerInfoUpdated: CustomerInfo.() -> Unit,
) : PurchasesDelegate {
    override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
        customerInfo.onCustomerInfoUpdated()
    }

    override fun onPurchasePromoProduct(
        product: StoreProduct,
        startPurchase: (
            onError: (error: PurchasesError, userCancelled: Boolean) -> Unit,
            onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit,
        ) -> Unit,
    ) = Unit
}
