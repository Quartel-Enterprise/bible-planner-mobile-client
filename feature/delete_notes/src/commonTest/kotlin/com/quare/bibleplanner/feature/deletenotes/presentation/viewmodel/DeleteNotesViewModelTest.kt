package com.quare.bibleplanner.feature.deletenotes.presentation.viewmodel

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.core.plan.domain.usecase.DeleteDayNotesUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.UpdateDayNotesUseCase
import com.quare.bibleplanner.core.plan.testing.FakeDayRepository
import com.quare.bibleplanner.core.plan.testing.NotesUpdate
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.feature.deletenotes.presentation.model.DeleteNotesUiEvent
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
internal class DeleteNotesViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: DeleteNotesViewModel
    private lateinit var dayRepository: FakeDayRepository
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a day with notes WHEN confirming the deletion THEN clears that day's notes and closes`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(DeleteNotesUiEvent.OnConfirmDelete)

            // Then
            assertEquals(
                listOf(
                    NotesUpdate(
                        weekNumber = WEEK,
                        dayNumber = DAY,
                        readingPlanType = ReadingPlanType.CHRONOLOGICAL,
                        notes = null,
                    ),
                ),
                dayRepository.notesUpdates,
            )
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        }

    @Test
    fun `GIVEN a day with notes WHEN confirming the deletion THEN tracks the deleted note with its day`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(DeleteNotesUiEvent.OnConfirmDelete)

            // Then
            assertEquals(
                listOf(
                    AnalyticsEventNames.NOTE_DELETED to mapOf<String, Any>(
                        AnalyticsParams.PLAN_TYPE to "chronological",
                        AnalyticsParams.WEEK_NUMBER to WEEK,
                        AnalyticsParams.DAY_NUMBER to DAY,
                    ),
                ),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN the confirmation WHEN cancelling THEN closes without deleting`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DeleteNotesUiEvent.OnCancel)

        // Then
        assertTrue(dayRepository.notesUpdates.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(listOf(AnalyticsEventNames.NOTE_DELETE_CANCELLED), trackedEvents.map { (name, _) -> name })
    }

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        dayRepository = FakeDayRepository(
            day = null,
            daysWithNotesCount = 0,
        )
        commands = mutableListOf()
        trackedEvents = mutableListOf()
        backgroundScope.launch { navigator.commands.collect(commands::add) }
        viewModel = DeleteNotesViewModel(
            deleteDayNotes = DeleteDayNotesUseCase(UpdateDayNotesUseCase(dayRepository)),
            navigator = navigator,
            route = DeleteNotesRoute(
                readingPlanType = ReadingPlanType.CHRONOLOGICAL.name,
                week = WEEK,
                day = DAY,
            ),
            trackEvent = { name, params -> trackedEvents += name to params },
        )
    }

    private companion object {
        const val WEEK = 3
        const val DAY = 5
    }
}
