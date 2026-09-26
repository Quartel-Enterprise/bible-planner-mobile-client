package com.quare.bibleplanner.feature.paywall.presentation.factory

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.month
import bibleplanner.feature.paywall.generated.resources.per_month
import bibleplanner.feature.paywall.generated.resources.per_year
import bibleplanner.feature.paywall.generated.resources.plan_annual
import bibleplanner.feature.paywall.generated.resources.plan_annual_description
import bibleplanner.feature.paywall.generated.resources.plan_monthly
import bibleplanner.feature.paywall.generated.resources.plan_monthly_description
import bibleplanner.feature.paywall.generated.resources.year
import com.quare.bibleplanner.core.plan.domain.usecase.GetMaxFreeNotesAmountUseCase
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackageType
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import com.quare.bibleplanner.feature.paywall.presentation.model.SubscriptionPlanPresentationModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class PaywallUiStateFactoryTest {
    private lateinit var factory: PaywallUiStateFactory

    private val monthlyPackage = StorePackage(
        identifier = "monthly",
        priceString = "$10.00",
        priceMicros = 10_000_000,
        title = "Monthly",
        description = "Monthly plan",
        type = StorePackageType.MONTHLY,
    )

    private val annualPackage = StorePackage(
        identifier = "annual",
        priceString = "$60.00",
        priceMicros = 60_000_000,
        title = "Annual",
        description = "Annual plan",
        type = StorePackageType.ANNUAL,
    )

    @Test
    fun `GIVEN monthly and annual packages WHEN creating THEN selects the annual plan with its savings`() = runTest {
        // Given
        prepareScenario(offerings = Result.success(listOf(monthlyPackage, annualPackage)))

        // When
        val result = factory.create(STORE_NAME)

        // Then
        assertEquals(
            expected = PaywallUiStateFactory.PaywallInitializationResult(
                uiState = PaywallUiState.Success(
                    subscriptionPlans = listOf(
                        SubscriptionPlanPresentationModel(
                            title = Res.string.plan_monthly,
                            description = Res.string.plan_monthly_description,
                            period = Res.string.per_month,
                            periodUnit = Res.string.month,
                            savePercentage = null,
                            isSelected = false,
                            priceDescription = "$10.00",
                            type = SubscriptionPlanType.Monthly,
                        ),
                        SubscriptionPlanPresentationModel(
                            title = Res.string.plan_annual,
                            description = Res.string.plan_annual_description,
                            period = Res.string.per_year,
                            periodUnit = Res.string.year,
                            savePercentage = 50,
                            isSelected = true,
                            priceDescription = "$60.00",
                            type = SubscriptionPlanType.Annual,
                        ),
                    ),
                    isPurchasing = false,
                    storeName = STORE_NAME,
                    maxFreeNotes = MAX_FREE_NOTES,
                ),
                storePackages = listOf(monthlyPackage, annualPackage),
            ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN only a monthly package WHEN creating THEN selects the first plan`() = runTest {
        // Given
        prepareScenario(offerings = Result.success(listOf(monthlyPackage)))

        // When
        val result = factory.create(STORE_NAME)

        // Then
        val state = assertIs<PaywallUiState.Success>(result.uiState)
        assertTrue(state.subscriptionPlans.single().isSelected)
        assertEquals(SubscriptionPlanType.Monthly, state.subscriptionPlans.single().type)
    }

    @Test
    fun `GIVEN an annual package without a monthly one WHEN creating THEN shows no savings`() = runTest {
        // Given
        prepareScenario(offerings = Result.success(listOf(annualPackage)))

        // When
        val result = factory.create(STORE_NAME)

        // Then
        val state = assertIs<PaywallUiState.Success>(result.uiState)
        assertNull(state.subscriptionPlans.single().savePercentage)
    }

    @Test
    fun `GIVEN a free monthly package WHEN creating THEN shows no savings on the annual plan`() = runTest {
        // Given
        prepareScenario(
            offerings = Result.success(listOf(monthlyPackage.copy(priceMicros = 0), annualPackage)),
        )

        // When
        val result = factory.create(STORE_NAME)

        // Then
        val state = assertIs<PaywallUiState.Success>(result.uiState)
        assertNull(state.subscriptionPlans.first { it.type == SubscriptionPlanType.Annual }.savePercentage)
    }

    @Test
    fun `GIVEN only unknown packages WHEN creating THEN returns the error state`() = runTest {
        // Given
        prepareScenario(
            offerings = Result.success(listOf(monthlyPackage.copy(type = StorePackageType.UNKNOWN))),
        )

        // When
        val result = factory.create(STORE_NAME)

        // Then
        assertEquals(PaywallUiStateFactory.PaywallInitializationResult(PaywallUiState.Error), result)
    }

    @Test
    fun `GIVEN the offerings fail to load WHEN creating THEN returns the error state`() = runTest {
        // Given
        prepareScenario(offerings = Result.failure(IllegalStateException("boom")))

        // When
        val result = factory.create(STORE_NAME)

        // Then
        assertEquals(PaywallUiStateFactory.PaywallInitializationResult(PaywallUiState.Error), result)
    }

    private fun prepareScenario(offerings: Result<List<StorePackage>>) {
        factory = PaywallUiStateFactory(
            getOfferingsResult = { offerings },
            getMaxFreeNotesAmount = GetMaxFreeNotesAmountUseCase(FakeGetIntRemoteConfig(MAX_FREE_NOTES)),
        )
    }

    private companion object {
        const val STORE_NAME = "Google Play Store"
        const val MAX_FREE_NOTES = 5
    }
}

private class FakeGetIntRemoteConfig(
    private val value: Int,
) : GetIntRemoteConfig {
    override suspend fun invoke(
        key: String,
        default: Int,
    ): Int = value
}
