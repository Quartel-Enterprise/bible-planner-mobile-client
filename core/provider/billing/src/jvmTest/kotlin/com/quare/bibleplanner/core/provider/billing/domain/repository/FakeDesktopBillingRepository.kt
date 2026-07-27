package com.quare.bibleplanner.core.provider.billing.domain.repository

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingUnavailableException
import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FakeDesktopBillingRepository(
    private val refreshesBeforePro: Int,
    private val checkoutUrl: String?,
) : DesktopBillingRepository {
    private val _subscriptionStatus: MutableStateFlow<SubscriptionStatus?> = MutableStateFlow(null)
    override val subscriptionStatus: StateFlow<SubscriptionStatus?> = _subscriptionStatus.asStateFlow()

    var refreshCount: Int = 0
        private set

    override suspend fun refreshSubscriptionStatus(): SubscriptionStatus {
        val status = if (refreshCount >= refreshesBeforePro) {
            SubscriptionStatus.Pro(
                planType = ProPlanType.MONTHLY,
                purchaseDate = null,
                expirationDate = null,
            )
        } else {
            SubscriptionStatus.Free
        }
        refreshCount++
        _subscriptionStatus.value = status
        return status
    }

    override fun clearSubscriptionStatus() {
        _subscriptionStatus.value = null
    }

    override suspend fun getStorePackages(): List<StorePackage> = error("unused")

    override fun getCheckoutUrl(
        appUserId: String,
        packageIdentifier: String,
    ): String = checkoutUrl?.let { url -> "$url/$appUserId?package_id=$packageIdentifier" }
        ?: throw BillingUnavailableException()
}
