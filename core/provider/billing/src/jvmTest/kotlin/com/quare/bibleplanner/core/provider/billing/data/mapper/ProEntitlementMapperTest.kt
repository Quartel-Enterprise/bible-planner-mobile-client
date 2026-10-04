package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.dto.TEST_MONTHLY_PRODUCT_ID
import com.quare.bibleplanner.core.provider.billing.data.dto.freeSubscriberResponse
import com.quare.bibleplanner.core.provider.billing.data.dto.proSubscriberResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class ProEntitlementMapperTest {
    private lateinit var mapper: ProEntitlementMapper

    @Test
    fun `GIVEN an entitlement expiring in the future WHEN mapping the active entitlement THEN returns it`() {
        // When
        val entitlement = mapper.mapActiveEntitlement(proSubscriberResponse())

        // Then
        assertNotNull(entitlement)
        assertEquals(
            expected = TEST_MONTHLY_PRODUCT_ID,
            actual = entitlement.productIdentifier,
        )
    }

    @Test
    fun `GIVEN an expired entitlement WHEN mapping the active entitlement THEN returns null`() {
        // When
        val entitlement = mapper.mapActiveEntitlement(
            proSubscriberResponse(expiresDate = "2026-01-06T22:23:11Z"),
        )

        // Then
        assertNull(entitlement)
    }

    @Test
    fun `GIVEN an expired entitlement in its grace period WHEN mapping the active entitlement THEN returns it`() {
        // When
        val entitlement = mapper.mapActiveEntitlement(
            proSubscriberResponse(
                expiresDate = "2026-01-06T22:23:11Z",
                gracePeriodExpiresDate = "2099-01-06T22:23:11Z",
            ),
        )

        // Then
        assertNotNull(entitlement)
    }

    @Test
    fun `GIVEN an entitlement without expiration date WHEN mapping the active entitlement THEN returns it`() {
        // When
        val entitlement = mapper.mapActiveEntitlement(proSubscriberResponse(expiresDate = null))

        // Then
        assertNotNull(entitlement)
    }

    @Test
    fun `GIVEN an unparseable expiration date WHEN mapping the active entitlement THEN returns null`() {
        // When
        val entitlement = mapper.mapActiveEntitlement(proSubscriberResponse(expiresDate = "not-a-date"))

        // Then
        assertNull(entitlement)
    }

    @Test
    fun `GIVEN a subscriber without entitlements WHEN mapping the active entitlement THEN returns null`() {
        // When
        val entitlement = mapper.mapActiveEntitlement(freeSubscriberResponse())

        // Then
        assertNull(entitlement)
    }

    @Test
    fun `GIVEN a subscription not unsubscribed WHEN mapping will renew THEN returns true`() {
        // When
        val willRenew = mapper.mapWillRenew(
            response = proSubscriberResponse(),
            productIdentifier = TEST_MONTHLY_PRODUCT_ID,
        )

        // Then
        assertEquals(
            expected = true,
            actual = willRenew,
        )
    }

    @Test
    fun `GIVEN an unsubscribed subscription WHEN mapping will renew THEN returns false`() {
        // When
        val willRenew = mapper.mapWillRenew(
            response = proSubscriberResponse(unsubscribeDetectedAt = "2026-01-06T18:24:02Z"),
            productIdentifier = TEST_MONTHLY_PRODUCT_ID,
        )

        // Then
        assertEquals(
            expected = false,
            actual = willRenew,
        )
    }

    @Test
    fun `GIVEN a product without a matching subscription WHEN mapping will renew THEN returns true`() {
        // When
        val willRenew = mapper.mapWillRenew(
            response = proSubscriberResponse(),
            productIdentifier = "promotional",
        )

        // Then
        assertEquals(
            expected = true,
            actual = willRenew,
        )
    }

    @BeforeTest
    fun setUp() {
        mapper = ProEntitlementMapper(EpochMillisMapper())
    }
}
