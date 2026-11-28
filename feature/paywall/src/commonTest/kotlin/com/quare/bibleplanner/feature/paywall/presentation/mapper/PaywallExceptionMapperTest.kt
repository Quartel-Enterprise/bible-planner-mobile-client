package com.quare.bibleplanner.feature.paywall.presentation.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallError
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PaywallExceptionMapperTest {
    private lateinit var mapper: PaywallExceptionMapper

    @BeforeTest
    fun setUp() {
        mapper = PaywallExceptionMapper()
    }

    @Test
    fun `GIVEN a user cancelled exception WHEN mapping THEN returns the purchase cancelled message`() {
        // When
        val message = mapper.map(BillingException.UserCancelled())

        // Then
        assertEquals(PaywallError.PURCHASE_CANCELLED, message)
    }

    @Test
    fun `GIVEN a network error WHEN mapping THEN returns the network message`() {
        // When
        val message = mapper.map(BillingException.NetworkError())

        // Then
        assertEquals(PaywallError.NETWORK, message)
    }

    @Test
    fun `GIVEN a pending payment WHEN mapping THEN returns the payment pending message`() {
        // When
        val message = mapper.map(BillingException.PaymentPending())

        // Then
        assertEquals(PaywallError.PAYMENT_PENDING, message)
    }

    @Test
    fun `GIVEN a failed restore WHEN mapping THEN returns the restore purchase message`() {
        // When
        val message = mapper.map(BillingException.RestorePurchaseFailed())

        // Then
        assertEquals(PaywallError.RESTORE_PURCHASE_FAILED, message)
    }

    @Test
    fun `GIVEN an unconfirmed browser checkout WHEN mapping THEN returns the browser checkout message`() {
        // When
        val message = mapper.map(BillingException.BrowserCheckoutNotConfirmed())

        // Then
        assertEquals(PaywallError.BROWSER_CHECKOUT_NOT_CONFIRMED, message)
    }

    @Test
    fun `GIVEN a non billing exception WHEN mapping THEN returns the unknown message`() {
        // When
        val message = mapper.map(IllegalStateException("boom"))

        // Then
        assertEquals(PaywallError.UNKNOWN, message)
    }
}
