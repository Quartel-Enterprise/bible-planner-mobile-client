package com.quare.bibleplanner.core.provider.billing.data.config

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val API_KEY = "rcb_test"
private const val PURCHASE_LINK = "https://pay.rev.cat/token"

internal class DesktopBillingConfigTest {
    @Test
    fun `should enable billing when both credentials are present`() {
        // Given
        val config = DesktopBillingConfig(
            apiKey = API_KEY,
            purchaseLink = PURCHASE_LINK,
        )

        // Then
        assertTrue(config.isEntitlementReadEnabled)
        assertTrue(config.isPurchaseEnabled)
    }

    @Test
    fun `should disable everything when the api key is missing`() {
        // Given
        val config = DesktopBillingConfig(
            apiKey = "",
            purchaseLink = PURCHASE_LINK,
        )

        // Then
        assertFalse(config.isEntitlementReadEnabled)
        assertFalse(config.isPurchaseEnabled)
    }

    @Test
    fun `should still read entitlements when only the purchase link is missing`() {
        // Given
        val config = DesktopBillingConfig(
            apiKey = API_KEY,
            purchaseLink = "",
        )

        // Then
        assertTrue(config.isEntitlementReadEnabled)
        assertFalse(config.isPurchaseEnabled)
    }
}
