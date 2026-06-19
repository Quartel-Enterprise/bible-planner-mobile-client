package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.source.BillingCustomerInfoSource
import com.quare.bibleplanner.core.provider.billing.mapper.toProEntitlement
import com.revenuecat.purchases.kmp.models.CustomerInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ObserveStoreSubscriptionStatusUseCase(
    private val customerInfoSource: BillingCustomerInfoSource,
    private val localDateTimeProvider: LocalDateTimeProvider,
) : ObserveStoreSubscriptionStatus {
    override fun invoke(): Flow<SubscriptionStatus> = customerInfoSource.customerInfo.map { it.toSubscriptionStatus() }

    private fun CustomerInfo.toSubscriptionStatus(): SubscriptionStatus {
        val entitlement = toProEntitlement()
        return if (entitlement != null) {
            val expirationDate = entitlement.expirationDateMillis?.let {
                localDateTimeProvider.getLocalDateTime(it)
            }
            val purchaseDate = entitlement.latestPurchaseDateMillis?.let {
                localDateTimeProvider.getLocalDateTime(it)
            }
            val planName = when (entitlement.productIdentifier) {
                "com.quare.bibleplanner.premium.monthly" -> "Pro / Monthly Plan"
                "com.quare.bibleplanner.premium.annual" -> "Pro / Annual Plan"
                else -> entitlement.productIdentifier
            }
            SubscriptionStatus.Pro(
                planName = planName,
                purchaseDate = purchaseDate,
                expirationDate = expirationDate,
                willRenew = entitlement.willRenew,
            )
        } else {
            SubscriptionStatus.Free
        }
    }
}
