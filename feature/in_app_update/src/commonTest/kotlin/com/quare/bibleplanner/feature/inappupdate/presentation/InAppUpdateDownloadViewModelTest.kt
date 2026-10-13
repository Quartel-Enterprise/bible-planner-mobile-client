package com.quare.bibleplanner.feature.inappupdate.presentation

import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateDownloadState
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class InAppUpdateDownloadViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: InAppUpdateDownloadViewModel
    private lateinit var downloadStates: MutableSharedFlow<UpdateDownloadState>
    private lateinit var trackedEvents: MutableList<String>
    private var installCalls = 0
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
    fun `GIVEN a running download WHEN it finishes THEN clears the progress and installs the update right away`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            downloadStates.emit(UpdateDownloadState.Downloading(progress = 99))

            // When
            downloadStates.emit(UpdateDownloadState.Downloaded)

            // Then
            assertNull(viewModel.downloadProgress.value)
            assertEquals(1, installCalls)
        }

    @Test
    fun `GIVEN a download that ended before the ViewModel existed WHEN it replays THEN does not install`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            downloadStates.emit(UpdateDownloadState.Downloaded)

            // Then
            assertNull(viewModel.downloadProgress.value)
            assertEquals(0, installCalls)
        }

    @Test
    fun `GIVEN a running download WHEN it fails THEN clears the progress tracks the failure and warns the user`() =
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
            assertEquals(0, installCalls)
        }

    @Test
    fun `GIVEN a running download WHEN it goes idle THEN clears the progress without installing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            downloadStates.emit(UpdateDownloadState.Downloading(progress = 10))

            // When
            downloadStates.emit(UpdateDownloadState.Idle)

            // Then
            assertNull(viewModel.downloadProgress.value)
            assertEquals(0, installCalls)
        }

    private fun TestScope.prepareScenario() {
        downloadStates = MutableSharedFlow()
        trackedEvents = mutableListOf()
        installCalls = 0
        actions = mutableListOf()
        viewModel = InAppUpdateDownloadViewModel(
            installDownloadedUpdate = { installCalls++ },
            trackEvent = { name, _ -> trackedEvents += name },
            observeUpdateDownloadState = { downloadStates },
        )
        backgroundScope.launch { viewModel.uiAction.collect(actions::add) }
    }
}
