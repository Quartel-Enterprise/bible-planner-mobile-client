package com.quare.bibleplanner.feature.logout.presentation.viewmodel

import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutFlushFailedException
import com.quare.bibleplanner.feature.logout.presentation.mapper.LogoutErrorMapper
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiAction
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiEvent
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class LogoutViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: LogoutViewModel
    private lateinit var actions: List<LogoutUiAction>
    private var requestedShouldFlush: Boolean? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a successful logout WHEN confirming THEN emits NavigateBack`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.success(Unit))

        // When
        viewModel.onEvent(LogoutUiEvent.OnConfirmLogout)

        // Then
        assertEquals(listOf(LogoutUiAction.NavigateBack), actions)
    }

    @Test
    fun `GIVEN a logout WHEN confirming THEN flushes pending changes`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.success(Unit))

        // When
        viewModel.onEvent(LogoutUiEvent.OnConfirmLogout)

        // Then
        assertEquals(true, requestedShouldFlush)
    }

    @Test
    fun `GIVEN a logout WHEN forcing THEN skips flushing pending changes`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.success(Unit))

        // When
        viewModel.onEvent(LogoutUiEvent.OnForceLogout)

        // Then
        assertEquals(false, requestedShouldFlush)
    }

    @Test
    fun `GIVEN a flush failure WHEN confirming THEN moves to pending changes error without a snackbar`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(result = Result.failure(LogoutFlushFailedException(IllegalStateException())))

            // When
            viewModel.onEvent(LogoutUiEvent.OnConfirmLogout)

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
            viewModel.onEvent(LogoutUiEvent.OnConfirmLogout)

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

    private fun TestScope.prepareScenario(result: Result<Unit>) {
        viewModel = LogoutViewModel(
            logout = { shouldFlush ->
                requestedShouldFlush = shouldFlush
                result
            },
            logoutErrorMapper = LogoutErrorMapper(),
        )
        actions = mutableListOf<LogoutUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}
