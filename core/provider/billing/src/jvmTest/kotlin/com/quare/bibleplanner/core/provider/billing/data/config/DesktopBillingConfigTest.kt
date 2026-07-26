package com.quare.bibleplanner.core.provider.billing.data.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val PRODUCTION_API_KEY = "rcb_production"
private const val PRODUCTION_PURCHASE_LINK = "https://pay.rev.cat/token"
private const val SANDBOX_API_KEY = "rcb_sb_sandbox"
private const val SANDBOX_PURCHASE_LINK = "https://pay.rev.cat/sandbox/token"

internal class DesktopBillingConfigTest {
    @Test
    fun `should use the sandbox credentials on a debug build`() {
        // Given
        val config = config(isDebugBuild = true)

        // Then
        assertEquals(
            expected = SANDBOX_API_KEY,
            actual = config.apiKey,
        )
        assertEquals(
            expected = SANDBOX_PURCHASE_LINK,
            actual = config.purchaseLink,
        )
    }

    @Test
    fun `should use the production credentials on a packaged build`() {
        // Given
        val config = config(isDebugBuild = false)

        // Then
        assertEquals(
            expected = PRODUCTION_API_KEY,
            actual = config.apiKey,
        )
        assertEquals(
            expected = PRODUCTION_PURCHASE_LINK,
            actual = config.purchaseLink,
        )
    }

    @Test
    fun `should enable billing when the active environment is fully configured`() {
        // Given
        val config = config(isDebugBuild = true)

        // Then
        assertTrue(config.isEntitlementReadEnabled)
        assertTrue(config.isPurchaseEnabled)
    }

    @Test
    fun `should disable billing when only the other environment is configured`() {
        // Given
        val config = config(
            isDebugBuild = true,
            sandboxApiKey = "",
            sandboxPurchaseLink = "",
        )

        // Then
        assertFalse(config.isEntitlementReadEnabled)
        assertFalse(config.isPurchaseEnabled)
    }

    @Test
    fun `should disable purchases when only the purchase link is missing`() {
        // Given
        val config = config(
            isDebugBuild = true,
            sandboxPurchaseLink = "",
        )

        // Then
        assertTrue(config.isEntitlementReadEnabled)
        assertFalse(config.isPurchaseEnabled)
    }

    private fun config(
        isDebugBuild: Boolean,
        sandboxApiKey: String = SANDBOX_API_KEY,
        sandboxPurchaseLink: String = SANDBOX_PURCHASE_LINK,
    ): DesktopBillingConfig = DesktopBillingConfig(
        productionApiKey = PRODUCTION_API_KEY,
        productionPurchaseLink = PRODUCTION_PURCHASE_LINK,
        sandboxApiKey = sandboxApiKey,
        sandboxPurchaseLink = sandboxPurchaseLink,
        isDebugBuild = isDebugBuild,
    )
}
