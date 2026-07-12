package com.quare.bibleplanner.feature.accountdetails.presentation.viewmodel

import com.quare.bibleplanner.core.devices.domain.usecase.RenameDevice
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.accountdetails.presentation.model.RenameDeviceUiEvent
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
        val viewModel = viewModel(currentName = "Old name")
        val backEvents = collectBack(viewModel)

        // When
        viewModel.onEvent(RenameDeviceUiEvent.OnConfirmClick("New name"))
        runCurrent()

        // Then
        assertEquals(listOf("row-1" to "New name"), renamedCalls)
        assertEquals(1, backEvents.size)
        assertTrue(trackedEvents.contains(AnalyticsEventNames.DEVICE_RENAMED))
    }

    @Test
    fun `GIVEN the unchanged name WHEN confirming THEN only navigates back`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(currentName = "Same")
        val backEvents = collectBack(viewModel)

        // When
        viewModel.onEvent(RenameDeviceUiEvent.OnConfirmClick("  Same  "))
        runCurrent()

        // Then
        assertTrue(renamedCalls.isEmpty())
        assertEquals(1, backEvents.size)
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a dismiss WHEN handling THEN tracks cancellation and navigates back`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(currentName = "Old")
        val backEvents = collectBack(viewModel)

        // When
        viewModel.onEvent(RenameDeviceUiEvent.OnDismiss)
        runCurrent()

        // Then
        assertTrue(renamedCalls.isEmpty())
        assertEquals(1, backEvents.size)
        assertTrue(trackedEvents.contains(AnalyticsEventNames.DEVICE_RENAME_CANCELLED))
    }

    private fun viewModel(currentName: String) = RenameDeviceViewModel(
        route = RenameDeviceNavRoute(deviceRowId = "row-1", currentName = currentName),
        renameDevice = RenameDevice { deviceRowId, name ->
            renamedCalls += deviceRowId to name
            Result.success(Unit)
        },
        trackEvent = TrackEvent { name, _ -> trackedEvents += name },
    )

    private fun TestScope.collectBack(viewModel: RenameDeviceViewModel): List<Unit> {
        val events = mutableListOf<Unit>()
        backgroundScope.launch { viewModel.backUiAction.collect { events += it } }
        return events
    }
}
