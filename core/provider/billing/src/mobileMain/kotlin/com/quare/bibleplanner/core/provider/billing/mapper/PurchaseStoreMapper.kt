package com.quare.bibleplanner.core.provider.billing.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore
import com.revenuecat.purchases.kmp.models.Store

internal fun Store.toPurchaseStore(): PurchaseStore? = when (this) {
    Store.APP_STORE, Store.MAC_APP_STORE -> PurchaseStore.APP_STORE
    Store.PLAY_STORE -> PurchaseStore.PLAY_STORE
    Store.RC_BILLING, Store.STRIPE -> PurchaseStore.WEB
    else -> null
}
