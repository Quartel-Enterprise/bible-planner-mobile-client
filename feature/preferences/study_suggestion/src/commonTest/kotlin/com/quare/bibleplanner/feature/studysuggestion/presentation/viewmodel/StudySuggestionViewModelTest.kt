package com.quare.bibleplanner.feature.studysuggestion.presentation.viewmodel

import bibleplanner.feature.preferences.study_suggestion.generated.resources.Res
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_disabled_message
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_enabled_message
import bibleplanner.feature.preferences.study_suggestion.generated.resources.study_suggestion_mode_blocked_message
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiAction
import com.quare.bibleplanner.feature.studysuggestion.presentation.model.StudySuggestionUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
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

internal class StudySuggestionViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: StudySuggestionViewModel
    private val navigator = Navigator()
    private lateinit var commands: List<NavigationCommand>
    private lateinit var actions: List<StudySuggestionUiAction>
    private lateinit var enabledCalls: MutableList<Boolean>
    private lateinit var modeCalls: MutableList<StudySuggestionMode>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN stored settings WHEN observing the state THEN exposes them as loaded`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            settings = StudySuggestionSettingsModel(
                isEnabled = true,
                mode = StudySuggestionMode.BANNER,
            ),
        )

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(
                StudySuggestionSettingsModel(
                    isEnabled = true,
                    mode = StudySuggestionMode.BANNER,
                ),
            ),
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `GIVEN the sheet WHEN turning the suggestion off THEN persists it and confirms with a snackbar`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(StudySuggestionUiEvent.OnToggleClick(false))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(false),
                actual = enabledCalls,
            )
            assertEquals(
                expected = StudySuggestionUiAction.ShowSnackbar(Res.string.study_suggestion_disabled_message),
                actual = actions.last(),
            )
        }

    @Test
    fun `GIVEN the sheet WHEN turning the suggestion on THEN persists it and confirms with a snackbar`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(StudySuggestionUiEvent.OnToggleClick(true))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(true),
                actual = enabledCalls,
            )
            assertEquals(
                expected = StudySuggestionUiAction.ShowSnackbar(Res.string.study_suggestion_enabled_message),
                actual = actions.last(),
            )
        }

    @Test
    fun `GIVEN the suggestion enabled WHEN picking a mode THEN persists the mode`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(StudySuggestionUiEvent.OnModeClick(StudySuggestionMode.BANNER))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(StudySuggestionMode.BANNER),
            actual = modeCalls,
        )
    }

    @Test
    fun `GIVEN the suggestion disabled WHEN tapping a mode THEN only explains how to unlock it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(StudySuggestionUiEvent.OnBlockedModeClick)
            runCurrent()

            // Then
            assertTrue(modeCalls.isEmpty())
            assertEquals(
                expected = StudySuggestionUiAction.ShowSnackbar(Res.string.study_suggestion_mode_blocked_message),
                actual = actions.last(),
            )
        }

    @Test
    fun `GIVEN the sheet WHEN dismissing THEN navigates back`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(StudySuggestionUiEvent.OnDismiss)
        runCurrent()

        // Then
        assertEquals(
            expected = NavigationCommand.NavigateBack,
            actual = commands.last(),
        )
    }

    private fun TestScope.prepareScenario(
        settings: StudySuggestionSettingsModel = StudySuggestionSettingsModel(
            isEnabled = true,
            mode = StudySuggestionMode.DIALOG,
        ),
    ) {
        enabledCalls = mutableListOf()
        modeCalls = mutableListOf()
        viewModel = StudySuggestionViewModel(
            observeStudySuggestionSettings = { flowOf(settings) },
            setStudySuggestionEnabled = { isEnabled -> enabledCalls += isEnabled },
            setStudySuggestionMode = { mode -> modeCalls += mode },
            navigator = navigator,
            trackEvent = { _, _ -> },
        )
        actions = mutableListOf<StudySuggestionUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        backgroundScope.launch { viewModel.uiState.collect { } }
    }
}
