package com.quare.bibleplanner.feature.deleteaccount.presentation.viewmodel

import bibleplanner.feature.delete_account.generated.resources.Res
import bibleplanner.feature.delete_account.generated.resources.delete_account_store_google_play
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.DeleteAccountPhase
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.DeleteAccountProgress
import com.quare.bibleplanner.feature.deleteaccount.presentation.mapper.StoreNameMapper
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiAction
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiEvent
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiState
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class DeleteAccountViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: DeleteAccountViewModel
    private lateinit var actions: List<DeleteAccountUiAction>
    private lateinit var states: List<DeleteAccountUiState>
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        trackedEvents.clear()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN no typed text WHEN starting THEN keeps the confirm button disabled`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = viewModel.uiState.value

        // Then
        assertFalse(state.isConfirmEnabled)
    }

    @Test
    fun `GIVEN the wrong text WHEN typing THEN keeps the confirm button disabled`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DeleteAccountUiEvent.OnConfirmationTextChange("EXCLU"))

        // Then
        assertFalse(viewModel.uiState.value.isConfirmEnabled)
    }

    @Test
    fun `GIVEN the keyword in a different case WHEN typing THEN enables the confirm button`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(DeleteAccountUiEvent.OnConfirmationTextChange(" delete "))

            // Then
            assertTrue(viewModel.uiState.value.isConfirmEnabled)
        }

    @Test
    fun `GIVEN the confirm button disabled WHEN confirming THEN does nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DeleteAccountUiEvent.OnConfirmDelete)

        // Then
        assertEquals(DeleteAccountUiStatus.Idle, viewModel.uiState.value.status)
        assertTrue(trackedEvents.isEmpty())
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `GIVEN a successful deletion WHEN confirming THEN reports both phases then the deleted status`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            typeKeyword()

            // When
            viewModel.onEvent(DeleteAccountUiEvent.OnConfirmDelete)
            advanceUntilIdle()

            // Then
            assertEquals(
                listOf(
                    DeleteAccountUiStatus.Deleting(DeleteAccountPhase.DELETING_DATA),
                    DeleteAccountUiStatus.Deleting(DeleteAccountPhase.CLOSING_ACCOUNT),
                    DeleteAccountUiStatus.Deleted,
                ),
                states.map(DeleteAccountUiState::status).filterNot { it == DeleteAccountUiStatus.Idle },
            )
        }

    @Test
    fun `GIVEN a successful deletion WHEN confirming THEN notifies success then emits NavigateBack`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            typeKeyword()

            // When
            viewModel.onEvent(DeleteAccountUiEvent.OnConfirmDelete)
            advanceUntilIdle()

            // Then
            assertEquals(2, actions.size)
            assertIs<DeleteAccountUiAction.NotifySuccess>(actions[0])
            assertEquals(DeleteAccountUiAction.NavigateBack, actions[1])
        }

    @Test
    fun `GIVEN a pro user WHEN confirming THEN tracks account_delete_confirmed as pro`() = runTest(testDispatcher) {
        // Given
        prepareScenario(subscriptionStatus = proSubscription(PurchaseStore.PLAY_STORE))
        typeKeyword()

        // When
        viewModel.onEvent(DeleteAccountUiEvent.OnConfirmDelete)
        advanceUntilIdle()

        // Then
        assertEquals(
            AnalyticsEventNames.ACCOUNT_DELETE_CONFIRMED to mapOf<String, Any>(AnalyticsParams.IS_PRO to true),
            trackedEvents.first(),
        )
    }

    @Test
    fun `GIVEN a failing request WHEN confirming THEN returns to idle and tracks delete_request`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(failingPhase = DeleteAccountPhase.DELETING_DATA)
            typeKeyword()

            // When
            viewModel.onEvent(DeleteAccountUiEvent.OnConfirmDelete)
            advanceUntilIdle()

            // Then
            assertEquals(DeleteAccountUiStatus.Idle, viewModel.uiState.value.status)
            assertIs<DeleteAccountUiAction.ShowSnackbar>(actions.single())
            assertEquals(
                AnalyticsEventNames.ACCOUNT_DELETE_FAILED to
                    mapOf<String, Any>(AnalyticsParams.REASON to "delete_request"),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN a failing session close WHEN confirming THEN tracks close_session`() = runTest(testDispatcher) {
        // Given
        prepareScenario(failingPhase = DeleteAccountPhase.CLOSING_ACCOUNT)
        typeKeyword()

        // When
        viewModel.onEvent(DeleteAccountUiEvent.OnConfirmDelete)
        advanceUntilIdle()

        // Then
        assertEquals(
            AnalyticsEventNames.ACCOUNT_DELETE_FAILED to
                mapOf<String, Any>(AnalyticsParams.REASON to "close_session"),
            trackedEvents.last(),
        )
    }

    @Test
    fun `GIVEN the idle dialog WHEN cancelling THEN emits NavigateBack`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DeleteAccountUiEvent.OnCancel)

        // Then
        assertEquals(listOf(DeleteAccountUiAction.NavigateBack), actions)
    }

    @Test
    fun `GIVEN a play store subscription WHEN starting THEN warns about that store`() = runTest(testDispatcher) {
        // Given
        prepareScenario(subscriptionStatus = proSubscription(PurchaseStore.PLAY_STORE))

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(Res.string.delete_account_store_google_play, state.storeNameRes)
    }

    @Test
    fun `GIVEN a promotional entitlement WHEN starting THEN shows no store warning`() = runTest(testDispatcher) {
        // Given
        prepareScenario(subscriptionStatus = proSubscription(store = null))

        // When
        val state = viewModel.uiState.value

        // Then
        assertTrue(state.isProUser)
        assertNull(state.storeNameRes)
    }

    @Test
    fun `GIVEN a free user WHEN starting THEN shows no store warning`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = viewModel.uiState.value

        // Then
        assertFalse(state.isProUser)
        assertNull(state.storeNameRes)
    }

    private fun proSubscription(store: PurchaseStore?): SubscriptionStatus.Pro = SubscriptionStatus.Pro(
        planType = ProPlanType.MONTHLY,
        purchaseDate = null,
        expirationDate = null,
        willRenew = true,
        store = store,
    )

    private fun typeKeyword() {
        viewModel.onEvent(DeleteAccountUiEvent.OnConfirmationTextChange(CONFIRMATION_KEYWORD))
    }

    private fun TestScope.prepareScenario(
        subscriptionStatus: SubscriptionStatus? = SubscriptionStatus.Free,
        failingPhase: DeleteAccountPhase? = null,
    ) {
        viewModel = DeleteAccountViewModel(
            deleteAccount = { fakeDeleteAccount(failingPhase) },
            getConfirmationKeyword = { CONFIRMATION_KEYWORD },
            getSubscriptionStatusFlow = { flowOf(subscriptionStatus) },
            storeNameMapper = StoreNameMapper(),
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        actions = mutableListOf<DeleteAccountUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        states = mutableListOf<DeleteAccountUiState>().also { collected ->
            backgroundScope.launch { viewModel.uiState.collect { collected += it } }
        }
    }

    private fun fakeDeleteAccount(failingPhase: DeleteAccountPhase?): Flow<DeleteAccountProgress> = flow {
        val failure = Result.failure<Unit>(IllegalStateException("boom"))
        emit(DeleteAccountProgress.InProgress(DeleteAccountPhase.DELETING_DATA))
        yield()
        if (failingPhase == DeleteAccountPhase.DELETING_DATA) {
            emit(DeleteAccountProgress.Finished(failure))
            return@flow
        }
        emit(DeleteAccountProgress.InProgress(DeleteAccountPhase.CLOSING_ACCOUNT))
        yield()
        emit(
            DeleteAccountProgress.Finished(
                if (failingPhase == DeleteAccountPhase.CLOSING_ACCOUNT) failure else Result.success(Unit),
            ),
        )
    }

    private companion object {
        const val CONFIRMATION_KEYWORD = "DELETE"
    }
}
