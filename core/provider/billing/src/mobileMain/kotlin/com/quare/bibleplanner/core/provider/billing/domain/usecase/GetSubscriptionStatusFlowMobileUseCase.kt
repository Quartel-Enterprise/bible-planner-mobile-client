package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionPurchasesDelegate
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.mapper.toProEntitlement
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.result.awaitCustomerInfoResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

internal class GetSubscriptionStatusFlowMobileUseCase(
    private val isProVerificationRequired: IsProVerificationRequiredUseCase,
    private val localDateTimeProvider: LocalDateTimeProvider,
) : GetSubscriptionStatusFlowUseCase {
    override fun invoke(): Flow<SubscriptionStatus> = callbackFlow {
        if (!isProVerificationRequired()) {
            send(
                SubscriptionStatus.Pro(
                    planName = "Enterprise Plan",
                    purchaseDate = null,
                    expirationDate = null,
                    willRenew = false,
                ),
            )
            close()
            return@callbackFlow
        }

        Purchases.sharedInstance.delegate = SubscriptionPurchasesDelegate { launch { send(toSubscriptionStatus()) } }

        Purchases.sharedInstance.awaitCustomerInfoResult().getOrNull()?.let {
            send(it.toSubscriptionStatus())
        }

        awaitClose {
            Purchases.sharedInstance.delegate = null
        }
    }

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
