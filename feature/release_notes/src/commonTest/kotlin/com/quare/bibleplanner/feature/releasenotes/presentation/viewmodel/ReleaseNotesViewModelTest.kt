package com.quare.bibleplanner.feature.releasenotes.presentation.viewmodel

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import com.quare.bibleplanner.feature.releasenotes.domain.usecase.GetReleaseNotesUseCase
import com.quare.bibleplanner.feature.releasenotes.presentation.factory.ReleaseNotesUiStateFactory
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiAction
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiEvent
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReleaseNotesViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ReleaseNotesViewModel
    private lateinit var actions: List<ReleaseNotesUiAction>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN release notes WHEN opening THEN shows them on the latest tab`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(ReleaseNotesTab.LATEST, assertIs<ReleaseNotesUiState.Success>(state).currentTab)
    }

    @Test
    fun `GIVEN the notes fail to load WHEN opening THEN shows the error state`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.failure(IllegalStateException("boom")))

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(ReleaseNotesUiState.Error, state)
    }

    @Test
    fun `GIVEN the latest tab WHEN selecting the past versions tab THEN switches to it and tracks it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ReleaseNotesUiEvent.OnTabSelected(ReleaseNotesTab.PAST_VERSIONS))

            // Then
            assertEquals(
                ReleaseNotesTab.PAST_VERSIONS,
                assertIs<ReleaseNotesUiState.Success>(viewModel.uiState.value).currentTab,
            )
            assertEquals(
                AnalyticsEventNames.RELEASE_NOTES_TAB_SELECTED to
                    mapOf<String, Any>(AnalyticsParams.TAB to "past_versions"),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN the error state WHEN selecting a tab THEN keeps the error state`() = runTest(testDispatcher) {
        // Given
        prepareScenario(result = Result.failure(IllegalStateException("boom")))

        // When
        viewModel.onEvent(ReleaseNotesUiEvent.OnTabSelected(ReleaseNotesTab.PAST_VERSIONS))

        // Then
        assertEquals(ReleaseNotesUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun `GIVEN the release notes WHEN going back THEN navigates back`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReleaseNotesUiEvent.OnBackClicked)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(AnalyticsEventNames.RELEASE_NOTES_BACK_CLICKED to emptyMap(), trackedEvents.single())
    }

    @Test
    fun `GIVEN the release notes WHEN opening all releases on GitHub THEN opens the releases page`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ReleaseNotesUiEvent.OnGithubAllReleasesClicked)

            // Then
            assertEquals(
                listOf<ReleaseNotesUiAction>(ReleaseNotesUiAction.OpenUrl(RELEASES_URL)),
                actions,
            )
        }

    @Test
    fun `GIVEN a version WHEN opening it on GitHub THEN opens the page of that release tag`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ReleaseNotesUiEvent.OnGithubVersionClicked("2.8.5"))

            // Then
            assertEquals(
                listOf<ReleaseNotesUiAction>(ReleaseNotesUiAction.OpenUrl("$RELEASES_URL/tag/2.8.5")),
                actions,
            )
            assertEquals(
                AnalyticsEventNames.GITHUB_RELEASE_OPENED to mapOf<String, Any>(AnalyticsParams.VERSION to "2.8.5"),
                trackedEvents.single(),
            )
        }

    private fun TestScope.prepareScenario(
        result: Result<List<ReleaseNoteModel>> = Result.success(
            listOf(
                ReleaseNoteModel(
                    version = "0.0.1",
                    changes = listOf("First change"),
                ),
            ),
        ),
    ) {
        val navigator = Navigator()
        val recordedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = recordedEvents
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        viewModel = ReleaseNotesViewModel(
            uiStateFactory = ReleaseNotesUiStateFactory(GetReleaseNotesUseCase { result }),
            navigator = navigator,
            platform = Platform.Android,
            trackEvent = { name, params -> recordedEvents += name to params },
        )
        actions = mutableListOf<ReleaseNotesUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }

    private companion object {
        const val RELEASES_URL = "https://github.com/Quartel-Enterprise/bible-planner-mobile-client/releases"
    }
}
