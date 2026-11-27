package com.quare.bibleplanner.feature.deleteaccount.presentation.mapper

import bibleplanner.feature.delete_account.generated.resources.Res
import bibleplanner.feature.delete_account.generated.resources.delete_account_store_app_store
import bibleplanner.feature.delete_account.generated.resources.delete_account_store_google_play
import bibleplanner.feature.delete_account.generated.resources.delete_account_store_web
import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore
import kotlin.test.Test
import kotlin.test.assertEquals

internal class StoreNameMapperTest {
    @Test
    fun `GIVEN each purchase store WHEN mapping it THEN names the store the user subscribed on`() {
        // Given
        val stores = PurchaseStore.entries

        // When
        val names = stores.associateWith(PurchaseStore::toStoreNameResource)

        // Then
        assertEquals(
            mapOf(
                PurchaseStore.APP_STORE to Res.string.delete_account_store_app_store,
                PurchaseStore.PLAY_STORE to Res.string.delete_account_store_google_play,
                PurchaseStore.WEB to Res.string.delete_account_store_web,
            ),
            names,
        )
    }
}
