package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.config.DesktopBillingConfig
import kotlin.test.Test
import kotlin.test.assertEquals

internal class WebPurchaseLinkBuilderTest {
    private lateinit var builder: WebPurchaseLinkBuilder

    @Test
    fun `should append the app user id and preselect the package`() {
        // Given
        prepareScenario("https://pay.rev.cat/token")

        // When
        val url = builder.build(
            appUserId = "user-1",
            packageIdentifier = $$"$rc_annual",
        )

        // Then
        assertEquals(
            expected = "https://pay.rev.cat/token/user-1?package_id=%24rc_annual",
            actual = url,
        )
    }

    @Test
    fun `should not duplicate the separator when the link ends with a slash`() {
        // Given
        prepareScenario("https://pay.rev.cat/token/")

        // When
        val url = builder.build(
            appUserId = "user-1",
            packageIdentifier = $$"$rc_monthly",
        )

        // Then
        assertEquals(
            expected = "https://pay.rev.cat/token/user-1?package_id=%24rc_monthly",
            actual = url,
        )
    }

    @Test
    fun `should encode an anonymous app user id`() {
        // Given
        prepareScenario("https://pay.rev.cat/token")

        // When
        val url = builder.build(
            appUserId = $$"$RCAnonymousID:abc123",
            packageIdentifier = $$"$rc_monthly",
        )

        // Then
        assertEquals(
            expected = "https://pay.rev.cat/token/%24RCAnonymousID%3Aabc123?package_id=%24rc_monthly",
            actual = url,
        )
    }

    private fun prepareScenario(purchaseLink: String) {
        builder = WebPurchaseLinkBuilder(
            DesktopBillingConfig(
                apiKey = "rcb_test",
                purchaseLink = purchaseLink,
            ),
        )
    }
}
