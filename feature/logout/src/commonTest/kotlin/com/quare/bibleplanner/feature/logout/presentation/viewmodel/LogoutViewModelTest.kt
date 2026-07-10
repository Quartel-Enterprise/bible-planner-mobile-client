package com.quare.bibleplanner.feature.logout.presentation.viewmodel

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutFlushFailedException
import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutPhase
import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutProgress
import com.quare.bibleplanner.feature.logout.presentation.mapper.LogoutErrorMapper
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiAction
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiEvent
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class LogoutViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: LogoutViewModel
    private lateinit var actions: List<LogoutUiAction>
    private lateinit var states: List<LogoutUiState>
    private var requestedShouldFlush: Boolean? = null
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a successful logout WHEN confirming THEN notifies success then emits NavigateBack`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(result = Result.success(Unit))

            // When
            viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout)

            // Then
            assertEquals(2, actions.size)
            assertIs<LogoutUiAction.NotifySuccess>(actions[0])
            assertEquals(LogoutUiAction.NavigateBack, actions[1])
        }

    @Test
    fun `GIVEN a logout WHEN confirming THEN flushes pending changes`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.success(Unit))

        // When
        viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout)

        // Then
        assertEquals(true, requestedShouldFlush)
    }

    @Test
    fun `GIVEN a logout WHEN forcing THEN skips flushing pending changes`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.success(Unit))

        // When
        viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnForceLogout)

        // Then
        assertEquals(false, requestedShouldFlush)
    }

    @Test
    fun `GIVEN a logout WHEN confirming THEN reports syncing then ending session`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.success(Unit))

        // When
        viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout)

        // Then
        val loadingPhases = states.filterIsInstance<LogoutUiState.Loading>().map(LogoutUiState.Loading::phase)
        assertEquals(listOf(LogoutPhase.SYNCING, LogoutPhase.ENDING_SESSION), loadingPhases)
    }

    @Test
    fun `GIVEN a logout WHEN forcing THEN reports ending session without syncing`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.success(Unit))

        // When
        viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnForceLogout)

        // Then
        val loadingPhases = states.filterIsInstance<LogoutUiState.Loading>().map(LogoutUiState.Loading::phase)
        assertEquals(listOf(LogoutPhase.ENDING_SESSION), loadingPhases)
    }

    @Test
    fun `GIVEN a flush failure WHEN confirming THEN moves to pending changes error without a snackbar`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(result = Result.failure(LogoutFlushFailedException(IllegalStateException())))

            // When
            viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout)

            // Then
            assertIs<LogoutUiState.PendingChangesError>(viewModel.uiState.value)
            assertTrue(actions.isEmpty())
        }

    @Test
    fun `GIVEN a non-flush failure WHEN confirming THEN resets to idle and shows a snackbar`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(result = Result.failure(IllegalStateException("boom")))

            // When
            viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout)

            // Then
            assertEquals(LogoutUiState.Idle, viewModel.uiState.value)
            assertEquals(1, actions.size)
            assertIs<LogoutUiAction.ShowSnackbar>(actions.first())
        }

    @Test
    fun `GIVEN a logout WHEN cancelling THEN emits NavigateBack`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.success(Unit))

        // When
        viewModel.onEvent(LogoutUiEvent.OnCancel)

        // Then
        assertEquals(listOf(LogoutUiAction.NavigateBack), actions)
    }

    @Test
    fun `GIVEN a logout WHEN confirming THEN tracks logout_confirmed as not forced`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.success(Unit))

        // When
        viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout)

        // Then
        assertEquals(
            listOf(AnalyticsEventNames.LOGOUT_CONFIRMED to mapOf<String, Any>(AnalyticsParams.IS_FORCED to false)),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN a logout WHEN forcing THEN tracks logout_confirmed as forced`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.success(Unit))

        // When
        viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnForceLogout)

        // Then
        assertEquals(
            listOf(AnalyticsEventNames.LOGOUT_CONFIRMED to mapOf<String, Any>(AnalyticsParams.IS_FORCED to true)),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN a flush failure WHEN confirming THEN tracks logout_failed with pending_changes`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(result = Result.failure(LogoutFlushFailedException(IllegalStateException())))

            // When
            viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout)

            // Then
            assertEquals(
                AnalyticsEventNames.LOGOUT_FAILED to mapOf<String, Any>(AnalyticsParams.REASON to "pending_changes"),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN a non-flush failure WHEN confirming THEN tracks logout_failed with unknown`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.failure(IllegalStateException("boom")))

        // When
        viewModel.onEvent(LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout)

        // Then
        assertEquals(
            AnalyticsEventNames.LOGOUT_FAILED to mapOf<String, Any>(AnalyticsParams.REASON to "unknown"),
            trackedEvents.last(),
        )
    }

    private fun TestScope.prepareScenario(result: Result<Unit>) {
        viewModel = LogoutViewModel(
            logout = { shouldFlush -> fakeLogout(shouldFlush, result) },
            logoutErrorMapper = LogoutErrorMapper(),
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        actions = mutableListOf<LogoutUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        states = mutableListOf<LogoutUiState>().also { collected ->
            backgroundScope.launch { viewModel.uiState.collect { collected += it } }
        }
    }

    /**
     * [yield] between emissions so the [states] collector observes each phase distinctly instead of
     * StateFlow conflating back-to-back updates — mirrors production, where real suspension (network
     * I/O in flushPendingChanges) separates the phases.
     */
    private fun fakeLogout(
        shouldFlush: Boolean,
        result: Result<Unit>,
    ): Flow<LogoutProgress> = flow {
        requestedShouldFlush = shouldFlush
        if (shouldFlush) {
            emit(LogoutProgress.InProgress(LogoutPhase.SYNCING))
            yield()
        }
        emit(LogoutProgress.InProgress(LogoutPhase.ENDING_SESSION))
        yield()
        emit(LogoutProgress.Finished(result))
    }
}
