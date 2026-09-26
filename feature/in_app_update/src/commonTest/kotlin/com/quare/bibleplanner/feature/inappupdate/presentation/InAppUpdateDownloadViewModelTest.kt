package com.quare.bibleplanner.feature.inappupdate.presentation

import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateDownloadState
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.UpdateDownloadedNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateDownloadUiAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class InAppUpdateDownloadViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: InAppUpdateDownloadViewModel
    private lateinit var downloadStates: MutableSharedFlow<UpdateDownloadState>
    private lateinit var trackedEvents: MutableList<String>
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var actions: MutableList<InAppUpdateDownloadUiAction>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a running download WHEN progress arrives THEN exposes the progress`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        downloadStates.emit(UpdateDownloadState.Downloading(progress = 42))

        // Then
        assertEquals(42, viewModel.downloadProgress.value)
    }

    @Test
    fun `GIVEN a running download WHEN it finishes THEN clears the progress and opens the restart prompt`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            downloadStates.emit(UpdateDownloadState.Downloading(progress = 99))

            // When
            downloadStates.emit(UpdateDownloadState.Downloaded)

            // Then
            assertNull(viewModel.downloadProgress.value)
            assertEquals(listOf<NavigationCommand>(NavigationCommand.Navigate(UpdateDownloadedNavRoute)), commands)
        }

    @Test
    fun `GIVEN a running download WHEN it fails THEN clears the progress, tracks it and warns the user`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            downloadStates.emit(UpdateDownloadState.Downloading(progress = 10))

            // When
            downloadStates.emit(UpdateDownloadState.Failed)

            // Then
            assertNull(viewModel.downloadProgress.value)
            assertEquals(listOf(AnalyticsEventNames.UPDATE_DOWNLOAD_FAILED), trackedEvents)
            assertEquals(listOf<InAppUpdateDownloadUiAction>(InAppUpdateDownloadUiAction.ShowDownloadFailed), actions)
        }

    @Test
    fun `GIVEN a running download WHEN it goes idle THEN clears the progress without navigating`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            downloadStates.emit(UpdateDownloadState.Downloading(progress = 10))

            // When
            downloadStates.emit(UpdateDownloadState.Idle)

            // Then
            assertNull(viewModel.downloadProgress.value)
            assertTrue(commands.isEmpty())
        }

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        downloadStates = MutableSharedFlow()
        trackedEvents = mutableListOf()
        commands = mutableListOf()
        actions = mutableListOf()
        backgroundScope.launch { navigator.commands.collect(commands::add) }
        viewModel = InAppUpdateDownloadViewModel(
            navigator = navigator,
            trackEvent = { name, _ -> trackedEvents += name },
            observeUpdateDownloadState = { downloadStates },
        )
        backgroundScope.launch { viewModel.uiAction.collect(actions::add) }
    }
}
