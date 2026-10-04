package com.quare.bibleplanner.feature.editplanstartdate.presentation.viewmodel

import com.quare.bibleplanner.core.date.ConvertUtcDateToLocalDateUseCase
import com.quare.bibleplanner.core.date.GetFinalTimestampAfterEditionUseCase
import com.quare.bibleplanner.core.date.toTimestampUTC
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.usecase.SetPlanStartTimeUseCase
import com.quare.bibleplanner.core.plan.testing.FakePlanRepository
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiEvent
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class EditPlanStartDateViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val today = LocalDate(
        year = 2024,
        month = 5,
        day = 1,
    )
    private lateinit var viewModel: EditPlanStartDateViewModel
    private lateinit var planRepository: FakePlanRepository
    private lateinit var commands: MutableList<NavigationCommand>
    private var loginNudgeRequests = 0

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a stored start date WHEN reading the opened dialog state THEN preselects the stored date`() =
        runTest(testDispatcher) {
            // Given
            val startDate = LocalDate(
                year = 2024,
                month = 1,
                day = 10,
            )
            prepareScenario(startDate = startDate)

            // When
            val uiState = viewModel.uiState.value

            // Then
            assertEquals(
                EditPlanStartDateUiState.Loaded(initialTimestamp = startDate.toTimestampUTC()),
                uiState,
            )
        }

    @Test
    fun `GIVEN no stored start date WHEN reading the opened dialog state THEN preselects today`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(startDate = null)

            // When
            val uiState = viewModel.uiState.value

            // Then
            assertEquals(
                EditPlanStartDateUiState.Loaded(initialTimestamp = today.toTimestampUTC()),
                uiState,
            )
        }

    @Test
    fun `GIVEN the dialog WHEN picking a date THEN saves its local midnight before closing and nudging login`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(startDate = null)
            val pickedDate = LocalDate(
                year = 2024,
                month = 6,
                day = 10,
            )

            // When
            viewModel.onEvent(EditPlanStartDateUiEvent.OnDateSelected(utcDateMillis = pickedDate.toTimestampUTC()))

            // Then
            assertEquals(
                listOf(pickedDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()),
                planRepository.startTimestamps,
            )
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(1, loginNudgeRequests)
        }

    @Test
    fun `GIVEN the dialog WHEN dismissing it THEN closes without saving`() = runTest(testDispatcher) {
        // Given
        prepareScenario(startDate = null)

        // When
        viewModel.onEvent(EditPlanStartDateUiEvent.OnDismissDialog)

        // Then
        assertTrue(planRepository.startTimestamps.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(0, loginNudgeRequests)
    }

    private fun TestScope.prepareScenario(startDate: LocalDate?) {
        val navigator = Navigator()
        planRepository = FakePlanRepository(
            plans = emptyMap(),
            startDate = startDate,
            selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
        )
        commands = mutableListOf()
        loginNudgeRequests = 0
        backgroundScope.launch { navigator.commands.collect(commands::add) }
        viewModel = EditPlanStartDateViewModel(
            planRepository = planRepository,
            setPlanStartTime = SetPlanStartTimeUseCase(
                planRepository = planRepository,
                currentTimestampProvider = { NOW },
            ),
            getFinalTimestampAfterEdition = GetFinalTimestampAfterEditionUseCase(),
            convertUtcDateToLocalDate = ConvertUtcDateToLocalDateUseCase(),
            currentTimestampProvider = { NOW },
            localDateTimeProvider = {
                LocalDateTime(
                    year = today.year,
                    month = today.month,
                    day = today.day,
                    hour = 10,
                    minute = 0,
                )
            },
            requestLoginNudgeIfNeeded = { loginNudgeRequests++ },
            navigator = navigator,
            trackEvent = { _, _ -> },
        )
    }

    private companion object {
        const val NOW = 1_714_557_600_000L
    }
}
