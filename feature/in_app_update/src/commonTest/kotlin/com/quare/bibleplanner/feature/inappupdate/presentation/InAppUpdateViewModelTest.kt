package com.quare.bibleplanner.feature.inappupdate.presentation

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.feature.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiEvent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiState
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class InAppUpdateViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: InAppUpdateViewModel
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var commands: MutableList<NavigationCommand>
    private var startUpdateCalls = 0

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a known version WHEN the prompt opens THEN shows it and tracks the prompt with the version`() =
        runTest(testDispatcher) {
            // When
            prepareScenario(versionName = VERSION_NAME)

            // Then
            assertEquals(InAppUpdateUiState(versionName = VERSION_NAME), viewModel.uiState.value)
            assertEquals(
                listOf(
                    AnalyticsEventNames.UPDATE_PROMPT_SHOWN to mapOf<String, Any>(
                        AnalyticsParams.SOURCE to UpdatePromptSource.MANUAL,
                        AnalyticsParams.VERSION to VERSION_NAME,
                    ),
                ),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN an unknown version WHEN the prompt opens THEN tracks the prompt without a version`() =
        runTest(testDispatcher) {
            // When
            prepareScenario(versionName = null)

            // Then
            assertEquals(
                listOf(
                    AnalyticsEventNames.UPDATE_PROMPT_SHOWN to
                        mapOf<String, Any>(AnalyticsParams.SOURCE to UpdatePromptSource.MANUAL),
                ),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN the prompt WHEN tapping update THEN starts the update, closes the prompt and tracks it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(versionName = VERSION_NAME)

            // When
            viewModel.onEvent(InAppUpdateUiEvent.OnUpdateClick)

            // Then
            assertEquals(1, startUpdateCalls)
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(
                AnalyticsEventNames.UPDATE_ACCEPTED to
                    mapOf<String, Any>(AnalyticsParams.SOURCE to UpdatePromptSource.MANUAL),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN the prompt WHEN dismissing it THEN closes the prompt without updating and tracks it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(versionName = VERSION_NAME)

            // When
            viewModel.onEvent(InAppUpdateUiEvent.OnDismiss)

            // Then
            assertEquals(0, startUpdateCalls)
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(
                AnalyticsEventNames.UPDATE_DISMISSED to
                    mapOf<String, Any>(AnalyticsParams.SOURCE to UpdatePromptSource.MANUAL),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN the prompt WHEN tapping update THEN tracks the acceptance only once`() = runTest(testDispatcher) {
        // Given
        prepareScenario(versionName = VERSION_NAME)

        // When
        viewModel.onEvent(InAppUpdateUiEvent.OnUpdateClick)

        // Then
        assertEquals(1, trackedEvents.count { (name, _) -> name == AnalyticsEventNames.UPDATE_ACCEPTED })
        assertFalse(trackedEvents.any { (name, _) -> name == AnalyticsEventNames.UPDATE_DISMISSED })
        assertTrue(trackedEvents.first().first == AnalyticsEventNames.UPDATE_PROMPT_SHOWN)
    }

    private fun TestScope.prepareScenario(versionName: String?) {
        val navigator = Navigator()
        trackedEvents = mutableListOf()
        commands = mutableListOf()
        startUpdateCalls = 0
        backgroundScope.launch { navigator.commands.collect(commands::add) }
        viewModel = InAppUpdateViewModel(
            startUpdate = { startUpdateCalls++ },
            navigator = navigator,
            route = InAppUpdateNavRoute(
                versionName = versionName,
                source = UpdatePromptSource.MANUAL,
            ),
            trackEvent = { name, params -> trackedEvents += name to params },
        )
    }

    private companion object {
        const val VERSION_NAME = "2.0.0"
    }
}
