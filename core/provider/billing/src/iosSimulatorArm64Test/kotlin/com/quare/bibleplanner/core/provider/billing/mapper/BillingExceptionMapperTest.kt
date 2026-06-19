package com.quare.bibleplanner.core.provider.billing.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BillingExceptionMapperTest {
    @Test
    fun `GIVEN a cancelled purchase error WHEN mapping THEN returns UserCancelled`() {
        // When
        val result = purchasesException(PurchasesErrorCode.PurchaseCancelledError).toBillingException()

        // Then
        assertIs<BillingException.UserCancelled>(result)
    }

    @Test
    fun `GIVEN a network error WHEN mapping THEN returns NetworkError`() {
        // When
        val result = purchasesException(PurchasesErrorCode.NetworkError).toBillingException()

        // Then
        assertIs<BillingException.NetworkError>(result)
    }

    @Test
    fun `GIVEN a payment pending error WHEN mapping THEN returns PaymentPending`() {
        // When
        val result = purchasesException(PurchasesErrorCode.PaymentPendingError).toBillingException()

        // Then
        assertIs<BillingException.PaymentPending>(result)
    }

    @Test
    fun `GIVEN any other purchases error WHEN mapping THEN returns Unknown`() {
        // When
        val result = purchasesException(PurchasesErrorCode.StoreProblemError).toBillingException()

        // Then
        assertIs<BillingException.Unknown>(result)
    }

    @Test
    fun `GIVEN a non-purchases throwable WHEN mapping THEN returns Unknown with the message`() {
        // When
        val result = IllegalStateException("boom").toBillingException()

        // Then
        assertIs<BillingException.Unknown>(result)
        assertEquals("boom", result.message)
    }

    private fun purchasesException(code: PurchasesErrorCode): PurchasesException =
        PurchasesException(PurchasesError(code))
}
