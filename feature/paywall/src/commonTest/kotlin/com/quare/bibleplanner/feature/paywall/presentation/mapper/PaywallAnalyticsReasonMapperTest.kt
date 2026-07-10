package com.quare.bibleplanner.feature.paywall.presentation.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PaywallAnalyticsReasonMapperTest {
    private lateinit var mapper: PaywallAnalyticsReasonMapper

    @BeforeTest
    fun setUp() {
        mapper = PaywallAnalyticsReasonMapper()
    }

    @Test
    fun `GIVEN a user cancelled exception WHEN mapping THEN returns user_cancelled`() {
        // When
        val reason = mapper.map(BillingException.UserCancelled())

        // Then
        assertEquals("user_cancelled", reason)
    }

    @Test
    fun `GIVEN a network error exception WHEN mapping THEN returns network`() {
        // When
        val reason = mapper.map(BillingException.NetworkError())

        // Then
        assertEquals("network", reason)
    }

    @Test
    fun `GIVEN a payment pending exception WHEN mapping THEN returns payment_pending`() {
        // When
        val reason = mapper.map(BillingException.PaymentPending())

        // Then
        assertEquals("payment_pending", reason)
    }

    @Test
    fun `GIVEN a restore purchase failed exception WHEN mapping THEN returns restore_failed`() {
        // When
        val reason = mapper.map(BillingException.RestorePurchaseFailed())

        // Then
        assertEquals("restore_failed", reason)
    }

    @Test
    fun `GIVEN an unknown billing exception WHEN mapping THEN returns unknown`() {
        // When
        val reason = mapper.map(BillingException.Unknown(message = "boom"))

        // Then
        assertEquals("unknown", reason)
    }

    @Test
    fun `GIVEN a non billing exception WHEN mapping THEN returns unknown`() {
        // When
        val reason = mapper.map(IllegalStateException("boom"))

        // Then
        assertEquals("unknown", reason)
    }
}
