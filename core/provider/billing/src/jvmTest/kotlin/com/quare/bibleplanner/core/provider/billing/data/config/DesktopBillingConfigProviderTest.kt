package com.quare.bibleplanner.core.provider.billing.data.config

import com.quare.bibleplanner.core.provider.billing.BuildKonfig
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DesktopBillingConfigProviderTest {
    @Test
    fun `GIVEN a debug build WHEN getting the config THEN provides the sandbox credentials`() {
        // Given
        val provider = DesktopBillingConfigProvider(isDebugBuild = true)

        // When
        val config = provider.getConfig()

        // Then
        assertEquals(
            expected = BuildKonfig.REVENUECAT_WEB_BILLING_SANDBOX_API_KEY,
            actual = config.apiKey,
        )
        assertEquals(
            expected = BuildKonfig.REVENUECAT_WEB_PURCHASE_LINK_SANDBOX,
            actual = config.purchaseLink,
        )
    }

    @Test
    fun `GIVEN a packaged build WHEN getting the config THEN provides the production credentials`() {
        // Given
        val provider = DesktopBillingConfigProvider(isDebugBuild = false)

        // When
        val config = provider.getConfig()

        // Then
        assertEquals(
            expected = BuildKonfig.REVENUECAT_WEB_BILLING_API_KEY,
            actual = config.apiKey,
        )
        assertEquals(
            expected = BuildKonfig.REVENUECAT_WEB_PURCHASE_LINK,
            actual = config.purchaseLink,
        )
    }
}
