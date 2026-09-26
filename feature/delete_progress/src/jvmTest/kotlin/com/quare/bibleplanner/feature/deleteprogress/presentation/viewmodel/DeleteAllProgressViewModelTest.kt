package com.quare.bibleplanner.feature.deleteprogress.presentation.viewmodel

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.bibleplanner.core.books.domain.usecase.ResetAllProgressUseCase
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiEvent
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class DeleteAllProgressViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val successFeedbackDuration = 700.milliseconds
    private lateinit var viewModel: DeleteAllProgressViewModel
    private lateinit var database: AppDatabase
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var trackedEvents: MutableList<String>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun `GIVEN the confirmation WHEN confirming THEN resets the progress and shows the success feedback`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(DeleteAllProgressUiEvent.OnConfirmDelete)
            runCurrent()

            // Then
            assertEquals(DeleteAllProgressUiState.Success, viewModel.uiState.value)
            assertEquals(listOf(AnalyticsEventNames.PROGRESS_RESET_CONFIRMED), trackedEvents)
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN a finished reset WHEN the success feedback ends THEN closes the dialog`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(DeleteAllProgressUiEvent.OnConfirmDelete)
        runCurrent()

        // When
        advanceTimeBy(successFeedbackDuration + 1.milliseconds)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
    }

    @Test
    fun `GIVEN the reset fails WHEN confirming THEN returns to idle without closing`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        database.close()

        // When
        viewModel.onEvent(DeleteAllProgressUiEvent.OnConfirmDelete)
        runCurrent()

        // Then
        assertEquals(DeleteAllProgressUiState.Idle, viewModel.uiState.value)
        assertTrue(trackedEvents.isEmpty())
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN the confirmation WHEN cancelling THEN closes the dialog and tracks the cancellation`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(DeleteAllProgressUiEvent.OnCancel)

            // Then
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(listOf(AnalyticsEventNames.PROGRESS_RESET_CANCELLED), trackedEvents)
        }

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        database = Room
            .inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(testDispatcher)
            .build()
        commands = mutableListOf()
        trackedEvents = mutableListOf()
        backgroundScope.launch { navigator.commands.collect(commands::add) }
        viewModel = DeleteAllProgressViewModel(
            resetAllProgress = ResetAllProgressUseCase(
                dayDao = database.dayDao(),
                bookDao = database.bookDao(),
                chapterDao = database.chapterDao(),
                verseDao = database.verseDao(),
                currentTimestampProvider = { NOW },
            ),
            navigator = navigator,
            trackEvent = { name, _ -> trackedEvents += name },
        )
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
