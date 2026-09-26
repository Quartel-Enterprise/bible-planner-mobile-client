package com.quare.bibleplanner.feature.inappupdate.presentation

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.feature.inappupdate.presentation.model.UpdateDownloadedUiEvent
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class UpdateDownloadedViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: UpdateDownloadedViewModel
    private lateinit var trackedEvents: MutableList<String>
    private lateinit var commands: MutableList<NavigationCommand>
    private var completeInstallCalls = 0

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a downloaded update WHEN tapping restart now THEN completes the install and tracks it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(UpdateDownloadedUiEvent.OnRestartNowClick)

            // Then
            assertEquals(1, completeInstallCalls)
            assertEquals(listOf(AnalyticsEventNames.UPDATE_INSTALL_STARTED), trackedEvents)
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN a downloaded update WHEN tapping later THEN closes the prompt without installing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(UpdateDownloadedUiEvent.OnLaterClick)

            // Then
            assertEquals(0, completeInstallCalls)
            assertEquals(listOf(AnalyticsEventNames.UPDATE_INSTALL_POSTPONED), trackedEvents)
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        }

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        trackedEvents = mutableListOf()
        commands = mutableListOf()
        completeInstallCalls = 0
        backgroundScope.launch { navigator.commands.collect(commands::add) }
        viewModel = UpdateDownloadedViewModel(
            completeUpdateInstall = { completeInstallCalls++ },
            navigator = navigator,
            trackEvent = { name, _ -> trackedEvents += name },
        )
    }
}
