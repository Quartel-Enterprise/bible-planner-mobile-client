package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore

internal class PurchaseStoreMapper {
    fun map(store: String?): PurchaseStore? = when (store) {
        APP_STORE, MAC_APP_STORE -> PurchaseStore.APP_STORE
        PLAY_STORE -> PurchaseStore.PLAY_STORE
        RC_BILLING, STRIPE -> PurchaseStore.WEB
        else -> null
    }

    private companion object {
        const val APP_STORE = "app_store"
        const val MAC_APP_STORE = "mac_app_store"
        const val PLAY_STORE = "play_store"
        const val RC_BILLING = "rc_billing"
        const val STRIPE = "stripe"
    }
}
