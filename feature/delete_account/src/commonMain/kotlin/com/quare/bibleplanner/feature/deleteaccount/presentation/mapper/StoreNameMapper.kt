package com.quare.bibleplanner.feature.deleteaccount.presentation.mapper

import bibleplanner.feature.delete_account.generated.resources.Res
import bibleplanner.feature.delete_account.generated.resources.delete_account_store_app_store
import bibleplanner.feature.delete_account.generated.resources.delete_account_store_google_play
import bibleplanner.feature.delete_account.generated.resources.delete_account_store_web
import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore
import org.jetbrains.compose.resources.StringResource

internal class StoreNameMapper {
    fun map(store: PurchaseStore?): StringResource? = when (store) {
        PurchaseStore.APP_STORE -> Res.string.delete_account_store_app_store
        PurchaseStore.PLAY_STORE -> Res.string.delete_account_store_google_play
        PurchaseStore.WEB -> Res.string.delete_account_store_web
        null -> null
    }
}
