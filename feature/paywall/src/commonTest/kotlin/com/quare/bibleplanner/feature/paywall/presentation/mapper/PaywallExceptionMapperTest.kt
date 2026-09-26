package com.quare.bibleplanner.feature.paywall.presentation.mapper

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.error_browser_checkout_not_confirmed
import bibleplanner.feature.paywall.generated.resources.error_network
import bibleplanner.feature.paywall.generated.resources.error_payment_pending
import bibleplanner.feature.paywall.generated.resources.error_purchase_cancelled
import bibleplanner.feature.paywall.generated.resources.error_restore_purchase
import bibleplanner.feature.paywall.generated.resources.error_unknown
import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
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
        assertEquals(Res.string.error_purchase_cancelled, message)
    }

    @Test
    fun `GIVEN a network error WHEN mapping THEN returns the network message`() {
        // When
        val message = mapper.map(BillingException.NetworkError())

        // Then
        assertEquals(Res.string.error_network, message)
    }

    @Test
    fun `GIVEN a pending payment WHEN mapping THEN returns the payment pending message`() {
        // When
        val message = mapper.map(BillingException.PaymentPending())

        // Then
        assertEquals(Res.string.error_payment_pending, message)
    }

    @Test
    fun `GIVEN a failed restore WHEN mapping THEN returns the restore purchase message`() {
        // When
        val message = mapper.map(BillingException.RestorePurchaseFailed())

        // Then
        assertEquals(Res.string.error_restore_purchase, message)
    }

    @Test
    fun `GIVEN an unconfirmed browser checkout WHEN mapping THEN returns the browser checkout message`() {
        // When
        val message = mapper.map(BillingException.BrowserCheckoutNotConfirmed())

        // Then
        assertEquals(Res.string.error_browser_checkout_not_confirmed, message)
    }

    @Test
    fun `GIVEN a non billing exception WHEN mapping THEN returns the unknown message`() {
        // When
        val message = mapper.map(IllegalStateException("boom"))

        // Then
        assertEquals(Res.string.error_unknown, message)
    }
}
