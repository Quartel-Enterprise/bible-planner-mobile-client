package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.quare.bibleplanner.core.provider.billing.domain.repository.FakeDesktopBillingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class GetRestorePurchaseResultDesktopUseCaseTest {
    private lateinit var useCase: GetRestorePurchaseResultDesktopUseCase

    @Test
    fun `GIVEN an account with an active entitlement WHEN restoring THEN succeeds`() = runTest {
        // Given
        prepareScenario(refreshesBeforePro = 0)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `GIVEN an account without an active entitlement WHEN restoring THEN fails`() = runTest {
        // Given
        prepareScenario(refreshesBeforePro = Int.MAX_VALUE)

        // When
        val result = useCase()

        // Then
        assertIs<BillingException.RestorePurchaseFailed>(result.exceptionOrNull())
    }

    private fun prepareScenario(refreshesBeforePro: Int) {
        useCase = GetRestorePurchaseResultDesktopUseCase(
            FakeDesktopBillingRepository(
                refreshesBeforePro = refreshesBeforePro,
                checkoutUrl = null,
            ),
        )
    }
}
