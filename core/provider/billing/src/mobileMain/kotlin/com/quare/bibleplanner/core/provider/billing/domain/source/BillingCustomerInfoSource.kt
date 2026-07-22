package com.quare.bibleplanner.core.provider.billing.domain.source

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionPurchasesDelegate
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.result.awaitCustomerInfoResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

internal class BillingCustomerInfoSource(
    private val purchases: Purchases,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val seedPolicies = listOf(
        CacheFetchPolicy.CACHED_OR_FETCHED,
        CacheFetchPolicy.FETCH_CURRENT,
    )

    val customerInfo: Flow<CustomerInfo> = callbackFlow {
        purchases.delegate = SubscriptionPurchasesDelegate { trySend(this) }
        seedPolicies.forEach { policy ->
            purchases.awaitCustomerInfoResult(policy).getOrNull()?.let(::trySend)
        }
        awaitClose { purchases.delegate = null }
    }.shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        replay = 1,
    )

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
