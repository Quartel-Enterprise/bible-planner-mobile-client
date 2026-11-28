package com.quare.bibleplanner.feature.paywall.presentation.utils

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.error_browser_checkout_not_confirmed
import bibleplanner.feature.paywall.generated.resources.error_network
import bibleplanner.feature.paywall.generated.resources.error_payment_pending
import bibleplanner.feature.paywall.generated.resources.error_purchase_cancelled
import bibleplanner.feature.paywall.generated.resources.error_restore_purchase
import bibleplanner.feature.paywall.generated.resources.error_unknown
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallError
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PaywallErrorTextTest {
    @Test
    fun `GIVEN each paywall error WHEN mapping it THEN picks its message`() {
        // Given
        val errors = PaywallError.entries

        // When
        val resources = errors.associateWith { error -> error.toStringResource() }

        // Then
        assertEquals(
            mapOf(
                PaywallError.PURCHASE_CANCELLED to Res.string.error_purchase_cancelled,
                PaywallError.NETWORK to Res.string.error_network,
                PaywallError.PAYMENT_PENDING to Res.string.error_payment_pending,
                PaywallError.RESTORE_PURCHASE_FAILED to Res.string.error_restore_purchase,
                PaywallError.BROWSER_CHECKOUT_NOT_CONFIRMED to Res.string.error_browser_checkout_not_confirmed,
                PaywallError.UNKNOWN to Res.string.error_unknown,
            ),
            resources,
        )
    }
}
