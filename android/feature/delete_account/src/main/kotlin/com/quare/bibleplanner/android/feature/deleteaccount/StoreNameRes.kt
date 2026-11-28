package com.quare.bibleplanner.android.feature.deleteaccount

import androidx.annotation.StringRes
import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore

@StringRes
internal fun PurchaseStore.toStoreNameRes(): Int = when (this) {
    PurchaseStore.APP_STORE -> R.string.delete_account_store_app_store
    PurchaseStore.PLAY_STORE -> R.string.delete_account_store_google_play
    PurchaseStore.WEB -> R.string.delete_account_store_web
}
