package com.quare.bibleplanner.feature.paywall.presentation.viewmodel

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.error_purchase_cancelled
import bibleplanner.feature.paywall.generated.resources.error_restore_purchase
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.CongratsNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetMaxFreeNotesAmountUseCase
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackageType
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.feature.paywall.domain.model.SubscriptionPlanType
import com.quare.bibleplanner.feature.paywall.presentation.factory.PaywallUiStateFactory
import com.quare.bibleplanner.feature.paywall.presentation.mapper.PaywallAnalyticsReasonMapper
import com.quare.bibleplanner.feature.paywall.presentation.mapper.PaywallExceptionMapper
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiAction
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiEvent
import com.quare.bibleplanner.feature.paywall.presentation.model.PaywallUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class PaywallViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PaywallViewModel
    private lateinit var actions: List<PaywallUiAction>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>
    private lateinit var purchasedPackages: List<StorePackage>
    private lateinit var isProUser: MutableStateFlow<Boolean>
    private var paywallImpressions = 0

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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN available offerings WHEN opening THEN tracks the view and shows the plans`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = viewModel.uiState.value

        // Then
        val success = assertIs<PaywallUiState.Success>(state)
        assertEquals("Google Play Store", success.storeName)
        assertEquals(
            AnalyticsEventNames.PAYWALL_VIEWED to mapOf<String, Any>(AnalyticsParams.SOURCE to "notes_limit"),
            trackedEvents.single(),
        )
        assertEquals(1, paywallImpressions)
    }

    @Test
    fun `GIVEN the desktop app WHEN opening THEN names Stripe as the store`() = runTest(testDispatcher) {
        // Given
        prepareScenario(platform = Platform.Desktop.Linux)

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals("Stripe", assertIs<PaywallUiState.Success>(state).storeName)
    }

    @Test
    fun `GIVEN failing offerings WHEN opening THEN shows the error state`() = runTest(testDispatcher) {
        // Given
        prepareScenario(offerings = Result.failure(IllegalStateException("boom")))

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(PaywallUiState.Error, state)
    }

    @Test
    fun `GIVEN the annual plan selected WHEN going back THEN tracks the dismissal with the plan and navigates back`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(PaywallUiEvent.OnBackClick)

            // Then
            assertEquals(
                AnalyticsEventNames.PAYWALL_DISMISSED to
                    mapOf<String, Any>(AnalyticsParams.SUBSCRIPTION_PLAN to "annual"),
                trackedEvents.last(),
            )
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        }

    @Test
    fun `GIVEN the paywall failed to load WHEN going back THEN tracks the dismissal without a plan`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(offerings = Result.failure(IllegalStateException("boom")))

            // When
            viewModel.onEvent(PaywallUiEvent.OnBackClick)

            // Then
            assertEquals(AnalyticsEventNames.PAYWALL_DISMISSED to emptyMap(), trackedEvents.last())
        }

    @Test
    fun `GIVEN the annual plan selected WHEN selecting the monthly plan THEN tracks it and moves the selection`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(PaywallUiEvent.OnPlanSelected(SubscriptionPlanType.Monthly))

            // Then
            val state = assertIs<PaywallUiState.Success>(viewModel.uiState.value)
            assertEquals(
                listOf(SubscriptionPlanType.Monthly),
                state.subscriptionPlans.filter { it.isSelected }.map { it.type },
            )
            assertEquals(
                AnalyticsEventNames.PAYWALL_PLAN_SELECTED to
                    mapOf<String, Any>(AnalyticsParams.SUBSCRIPTION_PLAN to "monthly"),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN the annual plan selected WHEN selecting it again THEN does not track a new selection`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(PaywallUiEvent.OnPlanSelected(SubscriptionPlanType.Annual))

            // Then
            assertEquals(listOf(AnalyticsEventNames.PAYWALL_VIEWED), trackedEvents.map { it.first })
        }

    @Test
    fun `GIVEN the paywall failed to load WHEN selecting a plan THEN keeps the error state`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(offerings = Result.failure(IllegalStateException("boom")))

            // When
            viewModel.onEvent(PaywallUiEvent.OnPlanSelected(SubscriptionPlanType.Monthly))

            // Then
            assertEquals(PaywallUiState.Error, viewModel.uiState.value)
        }

    @Test
    fun `GIVEN a logged out user WHEN starting the pro journey THEN asks to log in before purchasing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(userId = null)

            // When
            viewModel.onEvent(PaywallUiEvent.OnStartProJourneyClick)

            // Then
            assertEquals(
                listOf<NavigationCommand>(
                    NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.Purchase.key)),
                ),
                commands,
            )
            assertTrue(purchasedPackages.isEmpty())
        }

    @Test
    fun `GIVEN a successful purchase WHEN starting the pro journey THEN buys the selected plan and opens congrats`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(PaywallUiEvent.OnStartProJourneyClick)

            // Then
            assertEquals(listOf(annualPackage), purchasedPackages)
            val purchaseParams = mapOf<String, Any>(
                AnalyticsParams.SUBSCRIPTION_PLAN to "annual",
                AnalyticsParams.PACKAGE_ID to "annual",
                AnalyticsParams.PRICE to "$60.00",
                AnalyticsParams.STORE to "play_store",
            )
            assertEquals(
                listOf(
                    AnalyticsEventNames.PURCHASE_STARTED to purchaseParams,
                    AnalyticsEventNames.PURCHASE_COMPLETED to purchaseParams,
                ),
                trackedEvents.drop(1),
            )
            assertEquals(
                listOf<NavigationCommand>(NavigationCommand.NavigateReplacingTop(CongratsNavRoute)),
                commands,
            )
            assertFalse(assertIs<PaywallUiState.Success>(viewModel.uiState.value).isPurchasing)
        }

    @Test
    fun `GIVEN the monthly plan selected WHEN starting the pro journey THEN buys the monthly package`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(PaywallUiEvent.OnPlanSelected(SubscriptionPlanType.Monthly))

            // When
            viewModel.onEvent(PaywallUiEvent.OnStartProJourneyClick)

            // Then
            assertEquals(listOf(monthlyPackage), purchasedPackages)
        }

    @Test
    fun `GIVEN a cancelled purchase WHEN starting the pro journey THEN tracks the failure and shows a snackbar`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                platform = Platform.Ios,
                purchaseResult = { Result.failure(BillingException.UserCancelled()) },
            )

            // When
            viewModel.onEvent(PaywallUiEvent.OnStartProJourneyClick)

            // Then
            assertEquals(
                AnalyticsEventNames.PURCHASE_FAILED to mapOf<String, Any>(
                    AnalyticsParams.REASON to "user_cancelled",
                    AnalyticsParams.SUBSCRIPTION_PLAN to "annual",
                    AnalyticsParams.STORE to "app_store",
                ),
                trackedEvents.last(),
            )
            assertEquals(listOf(PaywallUiAction.ShowSnackbar(Res.string.error_purchase_cancelled)), actions)
            assertFalse(assertIs<PaywallUiState.Success>(viewModel.uiState.value).isPurchasing)
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN a purchase in progress WHEN starting the pro journey again THEN does not buy twice`() =
        runTest(testDispatcher) {
            // Given
            val pendingPurchase = CompletableDeferred<Result<Unit>>()
            prepareScenario(purchaseResult = { pendingPurchase.await() })
            viewModel.onEvent(PaywallUiEvent.OnStartProJourneyClick)

            // When
            viewModel.onEvent(PaywallUiEvent.OnStartProJourneyClick)

            // Then
            assertEquals(listOf(annualPackage), purchasedPackages)
            assertTrue(assertIs<PaywallUiState.Success>(viewModel.uiState.value).isPurchasing)
        }

    @Test
    fun `GIVEN no package matching the selected plan WHEN starting the pro journey THEN does not purchase`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(offerings = Result.success(listOf(annualPackage)))
            viewModel.onEvent(PaywallUiEvent.OnPlanSelected(SubscriptionPlanType.Monthly))

            // When
            viewModel.onEvent(PaywallUiEvent.OnStartProJourneyClick)

            // Then
            assertTrue(purchasedPackages.isEmpty())
        }

    @Test
    fun `GIVEN the paywall failed to load WHEN starting the pro journey THEN does not purchase`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(offerings = Result.failure(IllegalStateException("boom")))

            // When
            viewModel.onEvent(PaywallUiEvent.OnStartProJourneyClick)

            // Then
            assertTrue(purchasedPackages.isEmpty())
        }

    @Test
    fun `GIVEN a logged out user WHEN restoring purchases THEN asks to log in`() = runTest(testDispatcher) {
        // Given
        prepareScenario(userId = null)

        // When
        viewModel.onEvent(PaywallUiEvent.OnRestorePurchases)

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.Purchase.key)),
            ),
            commands,
        )
    }

    @Test
    fun `GIVEN a successful restore WHEN restoring purchases THEN tracks it and opens congrats`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(platform = Platform.Desktop.MacOs)

            // When
            viewModel.onEvent(PaywallUiEvent.OnRestorePurchases)

            // Then
            assertEquals(
                AnalyticsEventNames.RESTORE_COMPLETED to mapOf<String, Any>(AnalyticsParams.STORE to "desktop"),
                trackedEvents.last(),
            )
            assertEquals(
                listOf<NavigationCommand>(NavigationCommand.NavigateReplacingTop(CongratsNavRoute)),
                commands,
            )
            assertFalse(assertIs<PaywallUiState.Success>(viewModel.uiState.value).isPurchasing)
        }

    @Test
    fun `GIVEN a failed restore WHEN restoring purchases THEN shows a snackbar naming the store`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(restoreResult = Result.failure(BillingException.RestorePurchaseFailed()))

            // When
            viewModel.onEvent(PaywallUiEvent.OnRestorePurchases)

            // Then
            assertEquals(
                AnalyticsEventNames.RESTORE_FAILED to mapOf<String, Any>(
                    AnalyticsParams.REASON to "restore_failed",
                    AnalyticsParams.STORE to "play_store",
                ),
                trackedEvents.last(),
            )
            assertEquals(
                listOf(
                    PaywallUiAction.ShowSnackbar(
                        message = Res.string.error_restore_purchase,
                        args = listOf("Google Play Store"),
                    ),
                ),
                actions,
            )
            assertFalse(assertIs<PaywallUiState.Success>(viewModel.uiState.value).isPurchasing)
        }

    @Test
    fun `GIVEN the paywall failed to load WHEN a restore succeeds THEN still opens congrats`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(offerings = Result.failure(IllegalStateException("boom")))

            // When
            viewModel.onEvent(PaywallUiEvent.OnRestorePurchases)

            // Then
            assertEquals(
                listOf<NavigationCommand>(NavigationCommand.NavigateReplacingTop(CongratsNavRoute)),
                commands,
            )
            assertEquals(PaywallUiState.Error, viewModel.uiState.value)
        }

    @Test
    fun `GIVEN the paywall failed to load WHEN a restore fails THEN keeps the error state and shows a snackbar`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                offerings = Result.failure(IllegalStateException("boom")),
                restoreResult = Result.failure(BillingException.RestorePurchaseFailed()),
            )

            // When
            viewModel.onEvent(PaywallUiEvent.OnRestorePurchases)

            // Then
            assertEquals(PaywallUiState.Error, viewModel.uiState.value)
            assertEquals(1, actions.size)
        }

    @Test
    fun `GIVEN no purchase started WHEN the user becomes pro elsewhere THEN closes the paywall`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            isProUser.value = true

            // Then
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        }

    @Test
    fun `GIVEN a purchase started WHEN the user becomes pro THEN leaves the navigation to the purchase flow`() =
        runTest(testDispatcher) {
            // Given
            val pendingPurchase = CompletableDeferred<Result<Unit>>()
            prepareScenario(purchaseResult = { pendingPurchase.await() })
            viewModel.onEvent(PaywallUiEvent.OnStartProJourneyClick)

            // When
            isProUser.value = true

            // Then
            assertTrue(commands.isEmpty())
        }

    private fun TestScope.prepareScenario(
        platform: Platform = Platform.Android,
        userId: String? = "user-1",
        offerings: Result<List<StorePackage>> = Result.success(listOf(monthlyPackage, annualPackage)),
        purchaseResult: suspend () -> Result<Unit> = { Result.success(Unit) },
        restoreResult: Result<Unit> = Result.success(Unit),
    ) {
        val navigator = Navigator()
        val recordedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        val recordedPurchases = mutableListOf<StorePackage>()
        trackedEvents = recordedEvents
        purchasedPackages = recordedPurchases
        isProUser = MutableStateFlow(false)
        paywallImpressions = 0
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        viewModel = PaywallViewModel(
            getPurchaseResultUseCase = { storePackage ->
                recordedPurchases += storePackage
                purchaseResult()
            },
            getRestorePurchaseResultUseCase = { restoreResult },
            getAuthenticatedUserId = { userId },
            exceptionMapper = PaywallExceptionMapper(),
            analyticsReasonMapper = PaywallAnalyticsReasonMapper(),
            observeIsProUser = { isProUser },
            navigator = navigator,
            platform = platform,
            route = PaywallNavRoute(PaywallEntrySource.NOTES_LIMIT),
            factory = PaywallUiStateFactory(
                getOfferingsResult = { offerings },
                getMaxFreeNotesAmount = GetMaxFreeNotesAmountUseCase(FakeGetIntRemoteConfig()),
            ),
            trackCustomPaywallImpression = { paywallImpressions++ },
            trackEvent = { name, params -> recordedEvents += name to params },
        )
        actions = mutableListOf<PaywallUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}

private class FakeGetIntRemoteConfig : GetIntRemoteConfig {
    override suspend fun invoke(
        key: String,
        default: Int,
    ): Int = default
}
