package com.quare.bibleplanner.feature.subscriptiondetails.presentation.viewmodel

import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCase
import com.quare.bibleplanner.feature.subscriptiondetails.presentation.model.SubscriptionDetailsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class SubscriptionDetailsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: SubscriptionDetailsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a pro subscription WHEN observing the details THEN shows its plan and dates`() =
        runTest(testDispatcher) {
            // Given
            val purchaseDate = LocalDateTime(
                year = 2026,
                month = 1,
                day = 10,
                hour = 9,
                minute = 30,
            )
            val expirationDate = LocalDateTime(
                year = 2027,
                month = 1,
                day = 10,
                hour = 9,
                minute = 30,
            )
            prepareScenario(
                getSubscriptionStatusFlow = {
                    flowOf(
                        SubscriptionStatus.Pro(
                            planType = ProPlanType.ANNUAL,
                            purchaseDate = purchaseDate,
                            expirationDate = expirationDate,
                            willRenew = false,
                            store = PurchaseStore.APP_STORE,
                        ),
                    )
                },
            )

            // When
            val state = viewModel.uiState.value

            // Then
            assertEquals(
                SubscriptionDetailsUiState.Loaded(
                    planType = ProPlanType.ANNUAL,
                    purchaseDate = purchaseDate,
                    expirationDate = expirationDate,
                    willRenew = false,
                ),
                state,
            )
        }

    @Test
    fun `GIVEN a free user WHEN observing the details THEN shows the error state`() = runTest(testDispatcher) {
        // Given
        prepareScenario(getSubscriptionStatusFlow = { flowOf(SubscriptionStatus.Free) })

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(SubscriptionDetailsUiState.Error, state)
    }

    @Test
    fun `GIVEN billing is not available on the platform WHEN observing the details THEN shows the error state`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(getSubscriptionStatusFlow = null)

            // When
            val state = viewModel.uiState.value

            // Then
            assertEquals(SubscriptionDetailsUiState.Error, state)
        }

    private fun TestScope.prepareScenario(getSubscriptionStatusFlow: GetSubscriptionStatusFlowUseCase?) {
        viewModel = SubscriptionDetailsViewModel(getSubscriptionStatusFlow)
        backgroundScope.launch { viewModel.uiState.collect {} }
    }
}
