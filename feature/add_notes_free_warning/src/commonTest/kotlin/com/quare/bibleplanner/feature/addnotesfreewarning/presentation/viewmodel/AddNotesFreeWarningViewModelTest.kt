package com.quare.bibleplanner.feature.addnotesfreewarning.presentation.viewmodel

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningType
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class AddNotesFreeWarningViewModelTest {
    private lateinit var viewModel: AddNotesFreeWarningViewModel
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var trackedEvents: MutableList<String>

    @Test
    fun `GIVEN an opened notes limit warning WHEN reading its free notes limit THEN exposes the limit of the route`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val maxFreeNotesAmount = viewModel.maxFreeNotesAmount

            // Then
            assertEquals(MAX_FREE_NOTES, maxFreeNotesAmount)
        }

    @Test
    fun `GIVEN the warning WHEN subscribing to pro THEN replaces it with the notes limit paywall`() = runTest {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(AddNotesFreeWarningUiEvent.OnSubscribeToPro)

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.NavigateReplacingTop(PaywallNavRoute(PaywallEntrySource.NOTES_LIMIT)),
            ),
            commands,
        )
        assertEquals(listOf(AnalyticsEventNames.NOTES_LIMIT_SUBSCRIBE_CLICKED), trackedEvents)
    }

    @Test
    fun `GIVEN the verse notes warning WHEN subscribing to pro THEN replaces it with the verse notes limit paywall`() =
        runTest {
            // Given
            prepareScenario(type = AddNotesFreeWarningType.VERSE)

            // When
            viewModel.onEvent(AddNotesFreeWarningUiEvent.OnSubscribeToPro)

            // Then
            assertEquals(
                listOf<NavigationCommand>(
                    NavigationCommand.NavigateReplacingTop(PaywallNavRoute(PaywallEntrySource.VERSE_NOTES_LIMIT)),
                ),
                commands,
            )
        }

    @Test
    fun `GIVEN the warning WHEN cancelling THEN closes it`() = runTest {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(AddNotesFreeWarningUiEvent.OnCancel)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(listOf(AnalyticsEventNames.ADD_NOTES_FREE_WARNING_DISMISSED), trackedEvents)
    }

    private fun TestScope.prepareScenario(type: AddNotesFreeWarningType = AddNotesFreeWarningType.DAY) {
        val navigator = Navigator()
        commands = mutableListOf()
        trackedEvents = mutableListOf()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { navigator.commands.collect(commands::add) }
        viewModel = AddNotesFreeWarningViewModel(
            navigator = navigator,
            route = AddNotesFreeWarningNavRoute(
                maxFreeNotesAmount = MAX_FREE_NOTES,
                type = type,
            ),
            trackEvent = { name, _ -> trackedEvents += name },
        )
    }

    private companion object {
        const val MAX_FREE_NOTES = 3
    }
}
