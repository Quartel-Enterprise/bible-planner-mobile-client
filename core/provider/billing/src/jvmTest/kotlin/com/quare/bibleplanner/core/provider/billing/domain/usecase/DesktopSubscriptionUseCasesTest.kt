package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository
import com.quare.bibleplanner.core.provider.billing.domain.repository.FakeDesktopBillingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopSubscriptionUseCasesTest {
    private lateinit var repository: FakeDesktopBillingRepository

    @Test
    fun `GIVEN a pro subscriber WHEN observing the store status THEN refreshes and emits pro`() = runTest {
        // Given
        prepareScenario(refreshesBeforePro = 0)
        val useCase = ObserveStoreSubscriptionStatusDesktopUseCase(repository)

        // When
        val status = useCase().first()

        // Then
        assertIs<SubscriptionStatus.Pro>(status)
        assertEquals(
            expected = 1,
            actual = repository.refreshCount,
        )
    }

    @Test
    fun `GIVEN a cleared status WHEN observing the store status THEN treats it as free`() = runTest {
        // Given
        prepareScenario(refreshesBeforePro = 0)
        val useCase = ObserveStoreSubscriptionStatusDesktopUseCase(StatusClearedOnRefresh(repository))

        // When
        val status = useCase().first()

        // Then
        assertEquals(
            expected = SubscriptionStatus.Free,
            actual = status,
        )
    }

    @Test
    fun `GIVEN no cached status WHEN checking pro THEN refreshes it first`() = runTest {
        // Given
        prepareScenario(refreshesBeforePro = 0)
        val useCase = IsProUserDesktopUseCase(repository)

        // When
        val isPro = useCase()

        // Then
        assertTrue(isPro)
        assertEquals(
            expected = 1,
            actual = repository.refreshCount,
        )
    }

    @Test
    fun `GIVEN a cached free status WHEN checking pro THEN answers from the cache`() = runTest {
        // Given
        prepareScenario(refreshesBeforePro = 1)
        repository.refreshSubscriptionStatus()
        val useCase = IsProUserDesktopUseCase(repository)

        // When
        val isPro = useCase()

        // Then
        assertFalse(isPro)
        assertEquals(
            expected = 1,
            actual = repository.refreshCount,
        )
    }

    @Test
    fun `GIVEN a sign in WHEN logging the billing account in THEN refreshes the subscription`() = runTest {
        // Given
        prepareScenario(refreshesBeforePro = 0)
        val account = BillingUserAccountDesktop(repository)

        // When
        account.logIn("user-1")

        // Then
        assertIs<SubscriptionStatus.Pro>(repository.subscriptionStatus.value)
    }

    @Test
    fun `GIVEN a known subscription WHEN logging the billing account out THEN forgets it`() = runTest {
        // Given
        prepareScenario(refreshesBeforePro = 0)
        val account = BillingUserAccountDesktop(repository)
        account.logIn("user-1")
        account.setFirebaseAppInstanceId("instance-1")

        // When
        account.logOut()

        // Then
        assertNull(repository.subscriptionStatus.value)
    }

    private fun prepareScenario(refreshesBeforePro: Int) {
        repository = FakeDesktopBillingRepository(
            refreshesBeforePro = refreshesBeforePro,
            checkoutUrl = null,
        )
    }
}

private class StatusClearedOnRefresh(
    private val delegate: FakeDesktopBillingRepository,
) : DesktopBillingRepository by delegate {
    override suspend fun refreshSubscriptionStatus(): SubscriptionStatus = delegate.refreshSubscriptionStatus().also {
        delegate.clearSubscriptionStatus()
    }
}
