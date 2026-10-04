package com.quare.bibleplanner.core.provider.billing.data.config

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DesktopBillingConfigTest {
    @Test
    fun `GIVEN both credentials WHEN creating the config THEN enables billing`() {
        // Given
        val apiKey = API_KEY
        val purchaseLink = PURCHASE_LINK

        // When
        val config = DesktopBillingConfig(
            apiKey = apiKey,
            purchaseLink = purchaseLink,
        )

        // Then
        assertTrue(config.isEntitlementReadEnabled)
        assertTrue(config.isPurchaseEnabled)
    }

    @Test
    fun `GIVEN a missing api key WHEN creating the config THEN disables everything`() {
        // Given
        val apiKey = ""
        val purchaseLink = PURCHASE_LINK

        // When
        val config = DesktopBillingConfig(
            apiKey = apiKey,
            purchaseLink = purchaseLink,
        )

        // Then
        assertFalse(config.isEntitlementReadEnabled)
        assertFalse(config.isPurchaseEnabled)
    }

    @Test
    fun `GIVEN only a missing purchase link WHEN creating the config THEN still reads entitlements`() {
        // Given
        val apiKey = API_KEY
        val purchaseLink = ""

        // When
        val config = DesktopBillingConfig(
            apiKey = apiKey,
            purchaseLink = purchaseLink,
        )

        // Then
        assertTrue(config.isEntitlementReadEnabled)
        assertFalse(config.isPurchaseEnabled)
    }

    private companion object {
        const val API_KEY = "rcb_test"
        const val PURCHASE_LINK = "https://pay.rev.cat/token"
    }
}
