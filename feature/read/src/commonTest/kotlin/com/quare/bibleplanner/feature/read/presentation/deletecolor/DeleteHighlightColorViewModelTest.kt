package com.quare.bibleplanner.feature.read.presentation.deletecolor

import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.highlight_color_removed
import bibleplanner.feature.read.generated.resources.highlight_color_removed_kept
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.DeleteHighlightColorNavRoute
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import kotlinx.coroutines.Dispatchers
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

internal class DeleteHighlightColorViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val customColorKey = "c:120:50"
    private lateinit var viewModel: DeleteHighlightColorViewModel
    private lateinit var commands: List<NavigationCommand>
    private lateinit var actions: List<DeleteHighlightColorUiAction>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>
    private lateinit var removals: List<Pair<String, Boolean>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a custom color key WHEN opening the dialog THEN decodes the color it shows`() = runTest(testDispatcher) {
        // Given
        prepareScenario(colorKey = customColorKey)

        // When
        val color = viewModel.color

        // Then
        assertEquals(
            expected = HighlightColor.Custom(
                hue = 120,
                lightness = 50,
            ),
            actual = color,
        )
    }

    @Test
    fun `GIVEN an unknown color key WHEN opening the dialog THEN shows no color`() = runTest(testDispatcher) {
        // Given
        prepareScenario(colorKey = "unknown")

        // When
        val color = viewModel.color

        // Then
        assertNull(color)
    }

    @Test
    fun `GIVEN the dialog WHEN confirming and keeping the highlights THEN removes only the color`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(colorKey = customColorKey)

            // When
            viewModel.onEvent(DeleteHighlightColorUiEvent.OnConfirmClick(shouldKeepHighlights = true))

            // Then
            assertEquals(
                expected = listOf(customColorKey to true),
                actual = removals,
            )
            assertEquals(
                expected = listOf<DeleteHighlightColorUiAction>(
                    DeleteHighlightColorUiAction.NotifyDeletion(Res.string.highlight_color_removed_kept),
                ),
                actual = actions,
            )
            assertEquals(
                expected = listOf(
                    "highlight_custom_color_deleted" to mapOf<String, Any>(
                        "color" to customColorKey,
                        "kept_highlights" to true,
                    ),
                ),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN the dialog WHEN confirming without keeping the highlights THEN removes them with the color`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(colorKey = customColorKey)

            // When
            viewModel.onEvent(DeleteHighlightColorUiEvent.OnConfirmClick(shouldKeepHighlights = false))

            // Then
            assertEquals(
                expected = listOf(customColorKey to false),
                actual = removals,
            )
            assertEquals(
                expected = listOf<DeleteHighlightColorUiAction>(
                    DeleteHighlightColorUiAction.NotifyDeletion(Res.string.highlight_color_removed),
                ),
                actual = actions,
            )
        }

    @Test
    fun `GIVEN the dialog WHEN cancelling THEN goes back without removing anything`() = runTest(testDispatcher) {
        // Given
        prepareScenario(colorKey = customColorKey)

        // When
        viewModel.onEvent(DeleteHighlightColorUiEvent.OnCancelClick)

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
            actual = commands,
        )
        assertTrue(removals.isEmpty())
        assertEquals(
            expected = listOf("highlight_custom_color_delete_cancelled" to emptyMap<String, Any>()),
            actual = trackedEvents,
        )
    }

    private fun TestScope.prepareScenario(colorKey: String) {
        val navigator = Navigator()
        val collectedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        val collectedRemovals = mutableListOf<Pair<String, Boolean>>()
        trackedEvents = collectedEvents
        removals = collectedRemovals
        viewModel = DeleteHighlightColorViewModel(
            removeCustomHighlightColor = {
                key,
                shouldKeepHighlights,
                ->
                collectedRemovals += key to shouldKeepHighlights
            },
            navigator = navigator,
            route = DeleteHighlightColorNavRoute(colorKey = colorKey),
            trackEvent = { name, params -> collectedEvents += name to params },
        )
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        actions = mutableListOf<DeleteHighlightColorUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}
