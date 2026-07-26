package com.quare.bibleplanner.core.provider.billing.domain.repository

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import kotlinx.coroutines.flow.StateFlow

internal interface DesktopBillingRepository {
    val subscriptionStatus: StateFlow<SubscriptionStatus?>

    suspend fun refreshSubscriptionStatus(): SubscriptionStatus

    fun clearSubscriptionStatus()

    suspend fun getStorePackages(): List<StorePackage>

    fun getWebCheckoutUrl(
        appUserId: String,
        packageIdentifier: String,
    ): String
}
