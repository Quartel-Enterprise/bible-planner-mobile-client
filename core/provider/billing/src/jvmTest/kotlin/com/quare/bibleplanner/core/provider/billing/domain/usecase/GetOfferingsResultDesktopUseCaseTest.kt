package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackageType
import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetOfferingsResultDesktopUseCaseTest {
    private val monthly = StorePackage(
        identifier = "\$rc_monthly",
        priceString = "$4.99",
        priceMicros = 4_990_000L,
        title = "Monthly",
        description = "Pro monthly",
        type = StorePackageType.MONTHLY,
    )

    @Test
    fun `GIVEN store packages WHEN loading the offerings THEN succeeds with them`() = runTest {
        // Given
        val useCase = GetOfferingsResultDesktopUseCase(StorePackagesRepository { listOf(monthly) })

        // When
        val result = useCase()

        // Then
        assertEquals(
            expected = listOf(monthly),
            actual = result.getOrNull(),
        )
    }

    @Test
    fun `GIVEN the store is unreachable WHEN loading the offerings THEN fails`() = runTest {
        // Given
        val useCase = GetOfferingsResultDesktopUseCase(StorePackagesRepository { error("timeout") })

        // When
        val result = useCase()

        // Then
        val error = result.exceptionOrNull()
        assertIs<IllegalStateException>(error)
        assertEquals(
            expected = "timeout",
            actual = error.message,
        )
    }
}

private class StorePackagesRepository(
    private val loadPackages: () -> List<StorePackage>,
) : DesktopBillingRepository {
    override val subscriptionStatus: StateFlow<SubscriptionStatus?> get() = error("unused")

    override suspend fun refreshSubscriptionStatus(): SubscriptionStatus = error("unused")

    override fun clearSubscriptionStatus() = error("unused")

    override suspend fun getStorePackages(): List<StorePackage> = loadPackages()

    override fun getCheckoutUrl(
        appUserId: String,
        packageIdentifier: String,
    ): String = error("unused")
}
