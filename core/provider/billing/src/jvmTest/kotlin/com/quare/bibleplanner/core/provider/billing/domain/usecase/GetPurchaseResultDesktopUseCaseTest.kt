package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.quare.bibleplanner.core.provider.billing.domain.model.BillingUnavailableException
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackageType
import com.quare.bibleplanner.core.provider.billing.domain.repository.FakeDesktopBillingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private const val PURCHASE_LINK = "https://pay.rev.cat/token"
private const val APP_USER_ID = "user-1"

internal class GetPurchaseResultDesktopUseCaseTest {
    private lateinit var useCase: GetPurchaseResultDesktopUseCase
    private lateinit var openedUrls: MutableList<String>

    @Test
    fun `should open the web checkout and succeed once the entitlement becomes active`() = runTest {
        // Given
        prepareScenario(refreshesBeforePro = 2)

        // When
        val result = useCase(storePackage())

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = listOf($$"$$PURCHASE_LINK/$$APP_USER_ID?package_id=$rc_monthly"),
            actual = openedUrls,
        )
    }

    @Test
    fun `should fail as not confirmed when the entitlement never becomes active`() = runTest {
        // Given
        prepareScenario(refreshesBeforePro = Int.MAX_VALUE)

        // When
        val result = useCase(storePackage())

        // Then
        assertIs<BillingException.WebCheckoutNotConfirmed>(result.exceptionOrNull())
    }

    @Test
    fun `should fail without opening the browser when the purchase link is missing`() = runTest {
        // Given
        prepareScenario(checkoutUrl = null)

        // When
        val result = useCase(storePackage())

        // Then
        assertIs<BillingUnavailableException>(result.exceptionOrNull())
        assertTrue(openedUrls.isEmpty())
    }

    @Test
    fun `should fail without opening the browser when there is no authenticated user`() = runTest {
        // Given
        prepareScenario(authenticatedUserId = null)

        // When
        val result = useCase(storePackage())

        // Then
        assertIs<BillingUnavailableException>(result.exceptionOrNull())
        assertTrue(openedUrls.isEmpty())
    }

    private fun storePackage(): StorePackage = StorePackage(
        identifier = $$"$rc_monthly",
        priceString = "R$ 5,90",
        priceMicros = 5_900_000L,
        title = "Bible Planner Pro (Monthly)",
        description = "",
        type = StorePackageType.MONTHLY,
    )

    private fun prepareScenario(
        refreshesBeforePro: Int = 0,
        checkoutUrl: String? = PURCHASE_LINK,
        authenticatedUserId: String? = APP_USER_ID,
    ) {
        val repository = FakeDesktopBillingRepository(
            refreshesBeforePro = refreshesBeforePro,
            checkoutUrl = checkoutUrl,
        )
        openedUrls = mutableListOf()
        useCase = GetPurchaseResultDesktopUseCase(
            getAuthenticatedUserId = { authenticatedUserId },
            repository = repository,
            openUrl = { url -> openedUrls += url },
            awaitProEntitlement = AwaitProEntitlementUseCase(repository),
        )
    }
}
