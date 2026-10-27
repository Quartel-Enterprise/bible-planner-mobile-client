package com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel

import com.quare.bibleplanner.core.devices.domain.usecase.RenameDevice
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.accountdetails.presentation.model.RenameDeviceUiAction
import com.quare.bibleplanner.feature.accountdetails.presentation.model.RenameDeviceUiEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class RenameDeviceViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val renamedCalls = mutableListOf<Pair<String, String>>()
    private val trackedEvents = mutableListOf<String>()
    private val navigator = Navigator()
    private val recordingRenameDevice = RenameDevice { deviceRowId, name ->
        renamedCalls += deviceRowId to name
        Result.success(Unit)
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a new name WHEN confirming THEN renames and navigates back`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(
            currentName = "Old name",
            renameDevice = recordingRenameDevice,
        )
        val commands = collectCommands()

        // When
        viewModel.onEvent(RenameDeviceUiEvent.OnConfirmClick("New name"))
        runCurrent()

        // Then
        assertEquals(listOf("row-1" to "New name"), renamedCalls)
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertTrue(trackedEvents.contains(AnalyticsEventNames.DEVICE_RENAMED))
    }

    @Test
    fun `GIVEN the unchanged name WHEN confirming THEN only navigates back`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(
            currentName = "Same",
            renameDevice = recordingRenameDevice,
        )
        val commands = collectCommands()

        // When
        viewModel.onEvent(RenameDeviceUiEvent.OnConfirmClick("  Same  "))
        runCurrent()

        // Then
        assertTrue(renamedCalls.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a dismiss WHEN handling THEN tracks cancellation and navigates back`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(
            currentName = "Old",
            renameDevice = recordingRenameDevice,
        )
        val commands = collectCommands()

        // When
        viewModel.onEvent(RenameDeviceUiEvent.OnDismiss)
        runCurrent()

        // Then
        assertTrue(renamedCalls.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertTrue(trackedEvents.contains(AnalyticsEventNames.DEVICE_RENAME_CANCELLED))
    }

    @Test
    fun `GIVEN a rename in progress WHEN it has not finished THEN keeps the dialog open until it does`() =
        runTest(testDispatcher) {
            // Given
            val renameResult = CompletableDeferred<Result<Unit>>()
            val viewModel = viewModel(
                currentName = "Old name",
                renameDevice = RenameDevice { _, _ -> renameResult.await() },
            )
            val commands = collectCommands()
            viewModel.onEvent(RenameDeviceUiEvent.OnConfirmClick("New name"))
            runCurrent()
            assertTrue(commands.isEmpty())

            // When
            renameResult.complete(Result.success(Unit))
            runCurrent()

            // Then
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        }

    @Test
    fun `GIVEN a rename that fails WHEN confirming THEN notifies the failure and closes`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(
            currentName = "Old name",
            renameDevice = RenameDevice { _, _ -> Result.failure(IllegalStateException("disk full")) },
        )
        val commands = collectCommands()
        val actions = mutableListOf<RenameDeviceUiAction>()
        backgroundScope.launch { viewModel.uiAction.collect { actions += it } }

        // When
        viewModel.onEvent(RenameDeviceUiEvent.OnConfirmClick("New name"))
        runCurrent()

        // Then
        assertEquals(listOf<RenameDeviceUiAction>(RenameDeviceUiAction.NotifyRenameFailed), actions)
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
    }

    private fun viewModel(
        currentName: String,
        renameDevice: RenameDevice,
    ): RenameDeviceViewModel = RenameDeviceViewModel(
        route = RenameDeviceNavRoute(deviceRowId = "row-1", currentName = currentName),
        renameDevice = renameDevice,
        navigator = navigator,
        trackEvent = TrackEvent { name, _ -> trackedEvents += name },
    )

    private fun TestScope.collectCommands(): List<NavigationCommand> {
        val commands = mutableListOf<NavigationCommand>()
        backgroundScope.launch { navigator.commands.collect { commands += it } }
        return commands
    }
}
