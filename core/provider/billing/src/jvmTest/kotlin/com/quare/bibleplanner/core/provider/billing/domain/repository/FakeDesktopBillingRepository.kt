package com.quare.bibleplanner.core.provider.billing.domain.repository

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingUnavailableException
import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class FakeDesktopBillingRepository(
    private val refreshesBeforePro: Int,
    private val checkoutUrl: String?,
) : DesktopBillingRepository {
    override val subscriptionStatus: StateFlow<SubscriptionStatus?>
        field = MutableStateFlow<SubscriptionStatus?>(null)

    var refreshCount: Int = 0
        private set

    override suspend fun refreshSubscriptionStatus(): SubscriptionStatus {
        val status = if (refreshCount >= refreshesBeforePro) {
            SubscriptionStatus.Pro(
                planType = ProPlanType.MONTHLY,
                purchaseDate = null,
                expirationDate = null,
                willRenew = true,
                store = PurchaseStore.WEB,
            )
        } else {
            SubscriptionStatus.Free
        }
        refreshCount++
        subscriptionStatus.value = status
        return status
    }

    override fun clearSubscriptionStatus() {
        subscriptionStatus.value = null
    }

    override suspend fun getStorePackages(): List<StorePackage> = error("unused")

    override fun getCheckoutUrl(
        appUserId: String,
        packageIdentifier: String,
    ): String = checkoutUrl?.let { url -> "$url/$appUserId?package_id=$packageIdentifier" }
        ?: throw BillingUnavailableException()
}
