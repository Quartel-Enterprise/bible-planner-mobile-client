package com.quare.bibleplanner.core.provider.billing

import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import kotlinx.coroutines.flow.StateFlow

interface BillingService {
    val isPremium: StateFlow<Boolean>

    suspend fun getOfferings(): Result<List<StorePackage>>

    suspend fun purchase(storePackage: StorePackage): Result<Unit>

    suspend fun restorePurchases(): Result<Unit>
}
