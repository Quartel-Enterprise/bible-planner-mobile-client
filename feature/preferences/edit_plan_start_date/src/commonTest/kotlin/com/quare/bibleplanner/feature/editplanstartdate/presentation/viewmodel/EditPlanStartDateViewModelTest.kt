package com.quare.bibleplanner.feature.editplanstartdate.presentation.viewmodel

import com.quare.bibleplanner.core.date.GetFinalTimestampAfterEditionUseCase
import com.quare.bibleplanner.core.date.toTimestampUTC
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.plan.domain.usecase.SetPlanStartTimeUseCase
import com.quare.bibleplanner.feature.editplanstartdate.domain.usecase.ConvertUtcDateToLocalDateUseCase
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiEvent
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    fun `GIVEN a stored start date WHEN the dialog opens THEN preselects the stored date`() = runTest(testDispatcher) {
        // Given
        val startDate = LocalDate(
            year = 2024,
            month = 1,
            day = 10,
        )

        // When
        prepareScenario(startDate = startDate)

        // Then
        assertEquals(
            EditPlanStartDateUiState.Loaded(initialTimestamp = startDate.toTimestampUTC()),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `GIVEN no stored start date WHEN the dialog opens THEN preselects today`() = runTest(testDispatcher) {
        // When
        prepareScenario(startDate = null)

        // Then
        assertEquals(
            EditPlanStartDateUiState.Loaded(initialTimestamp = today.toTimestampUTC()),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `GIVEN the dialog WHEN picking a date THEN saves its local midnight, closes and nudges login`() =
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
                planRepository.savedTimestamps,
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
        assertTrue(planRepository.savedTimestamps.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(0, loginNudgeRequests)
    }

    private fun TestScope.prepareScenario(startDate: LocalDate?) {
        val navigator = Navigator()
        planRepository = FakePlanRepository(startDate)
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

private class FakePlanRepository(
    startDate: LocalDate?,
) : PlanRepository {
    private val startDate = MutableStateFlow(startDate)
    val savedTimestamps = mutableListOf<Long>()

    override fun getStartPlanTimestamp(): Flow<LocalDate?> = startDate

    override suspend fun setStartPlanTimestamp(timestamp: Long) {
        savedTimestamps += timestamp
    }

    override suspend fun getPlans(readingPlanType: ReadingPlanType): List<WeekPlanModel> = error("unused")

    override fun getSelectedReadingPlanFlow(): Flow<ReadingPlanType> = error("unused")

    override suspend fun setSelectedReadingPlan(readingPlanType: ReadingPlanType) = error("unused")

    override suspend fun seedDefaultStartDate(timestamp: Long) = error("unused")
}
