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
    private lateinit var viewModel: RenameDeviceViewModel
    private lateinit var renamedCalls: List<Pair<String, String>>
    private lateinit var trackedEvents: List<String>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var actions: List<RenameDeviceUiAction>

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
        prepareScenario(currentName = "Old name")

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
        prepareScenario(currentName = "Same")

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
        prepareScenario(currentName = "Old")

        // When
        viewModel.onEvent(RenameDeviceUiEvent.OnDismiss)
        runCurrent()

        // Then
        assertTrue(renamedCalls.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertTrue(trackedEvents.contains(AnalyticsEventNames.DEVICE_RENAME_CANCELLED))
    }

    @Test
    fun `GIVEN a rename that has not finished WHEN confirming THEN keeps the dialog open`() = runTest(testDispatcher) {
        // Given
        val pendingRename = CompletableDeferred<Result<Unit>>()
        prepareScenario(
            currentName = "Old name",
            renameResult = { pendingRename.await() },
        )

        // When
        viewModel.onEvent(RenameDeviceUiEvent.OnConfirmClick("New name"))
        runCurrent()

        // Then
        assertEquals(listOf("row-1" to "New name"), renamedCalls)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN a rename that fails WHEN confirming THEN notifies the failure and closes`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            currentName = "Old name",
            renameResult = { Result.failure(IllegalStateException("disk full")) },
        )

        // When
        viewModel.onEvent(RenameDeviceUiEvent.OnConfirmClick("New name"))
        runCurrent()

        // Then
        assertEquals(listOf<RenameDeviceUiAction>(RenameDeviceUiAction.NotifyRenameFailed), actions)
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
    }

    private fun TestScope.prepareScenario(
        currentName: String,
        renameResult: suspend () -> Result<Unit> = { Result.success(Unit) },
    ) {
        val navigator = Navigator()
        val recordedRenames = mutableListOf<Pair<String, String>>()
        val recordedEvents = mutableListOf<String>()
        renamedCalls = recordedRenames
        trackedEvents = recordedEvents
        viewModel = RenameDeviceViewModel(
            route = RenameDeviceNavRoute(
                deviceRowId = "row-1",
                currentName = currentName,
            ),
            renameDevice = RenameDevice { deviceRowId, name ->
                recordedRenames += deviceRowId to name
                renameResult()
            },
            navigator = navigator,
            trackEvent = TrackEvent { name, _ -> recordedEvents += name },
        )
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        actions = mutableListOf<RenameDeviceUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}
