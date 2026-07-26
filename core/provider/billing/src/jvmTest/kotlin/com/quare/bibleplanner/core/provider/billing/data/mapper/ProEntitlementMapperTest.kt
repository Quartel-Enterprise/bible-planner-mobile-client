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
    fun `should return the entitlement when it expires in the future`() {
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
    fun `should return no entitlement when it already expired`() {
        // When
        val entitlement = mapper.mapActiveEntitlement(
            proSubscriberResponse(expiresDate = "2026-01-06T22:23:11Z"),
        )

        // Then
        assertNull(entitlement)
    }

    @Test
    fun `should return the entitlement when only the grace period is still running`() {
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
    fun `should return the entitlement when it has no expiration date`() {
        // When
        val entitlement = mapper.mapActiveEntitlement(proSubscriberResponse(expiresDate = null))

        // Then
        assertNotNull(entitlement)
    }

    @Test
    fun `should return no entitlement when the expiration date is unparseable`() {
        // When
        val entitlement = mapper.mapActiveEntitlement(proSubscriberResponse(expiresDate = "not-a-date"))

        // Then
        assertNull(entitlement)
    }

    @Test
    fun `should return no entitlement when the subscriber has none`() {
        // When
        val entitlement = mapper.mapActiveEntitlement(freeSubscriberResponse())

        // Then
        assertNull(entitlement)
    }

    @Test
    fun `should renew when the subscription was not unsubscribed`() {
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
    fun `should not renew when the subscription was unsubscribed`() {
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
    fun `should renew when the product has no matching subscription`() {
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
