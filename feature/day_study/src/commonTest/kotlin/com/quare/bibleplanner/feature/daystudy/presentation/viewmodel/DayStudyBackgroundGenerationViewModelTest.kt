package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.FakeDayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyBackgroundGenerationUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyBackgroundGenerationUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
internal class DayStudyBackgroundGenerationViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a backgrounded job WHEN observing THEN it is shown`() = runTest(testDispatcher) {
        // Given
        val coordinator = FakeDayStudyGenerationCoordinator()
        val viewModel = viewModel(coordinator)
        val job = generatingJob(dayRoute)

        // When
        coordinator.jobsFlow.value = listOf(job)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isVisible)
        assertEquals(listOf(job), state.jobs)
    }

    @Test
    fun `GIVEN the active day's job WHEN observing THEN it is suppressed`() = runTest(testDispatcher) {
        // Given
        val coordinator = FakeDayStudyGenerationCoordinator()
        val viewModel = viewModel(coordinator)
        val job = generatingJob(dayRoute)

        // When
        coordinator.jobsFlow.value = listOf(job)
        coordinator.activeKeyFlow.value = job.key
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isVisible)
    }

    @Test
    fun `GIVEN a dismissed job WHEN observing THEN it is suppressed`() = runTest(testDispatcher) {
        // Given
        val coordinator = FakeDayStudyGenerationCoordinator()
        val viewModel = viewModel(coordinator)
        val job = generatingJob(dayRoute)

        // When
        coordinator.jobsFlow.value = listOf(job)
        coordinator.dismissedKeysFlow.value = setOf(job.key)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isVisible)
    }

    @Test
    fun `GIVEN a failed job WHEN observing THEN it is suppressed`() = runTest(testDispatcher) {
        // Given
        val coordinator = FakeDayStudyGenerationCoordinator()
        val viewModel = viewModel(coordinator)
        val failedJob = generatingJob(dayRoute).copy(
            status = DayStudyGenerationStatus.Failed(
                isLimitReached = false,
                isOffline = false,
            ),
        )

        // When
        coordinator.jobsFlow.value = listOf(failedJob)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isVisible)
    }

    @Test
    fun `GIVEN a shown job WHEN it goes away THEN it hides but keeps the last jobs for the exit animation`() =
        runTest(testDispatcher) {
            // Given
            val coordinator = FakeDayStudyGenerationCoordinator()
            val viewModel = viewModel(coordinator)
            val job = generatingJob(dayRoute)
            coordinator.jobsFlow.value = listOf(job)
            advanceUntilIdle()

            // When
            coordinator.jobsFlow.value = emptyList()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isVisible)
            assertEquals(listOf(job), state.jobs)
        }

    @Test
    fun `WHEN open is clicked THEN requests open and navigates to the day route`() = runTest(testDispatcher) {
        // Given
        val coordinator = FakeDayStudyGenerationCoordinator()
        val viewModel = viewModel(coordinator)
        val job = generatingJob(dayRoute)
        val actions = mutableListOf<DayStudyBackgroundGenerationUiAction>()
        backgroundScope.launch { viewModel.uiAction.collect { actions += it } }

        // When
        viewModel.onEvent(DayStudyBackgroundGenerationUiEvent.OnOpenClick(job))
        advanceUntilIdle()

        // Then
        assertEquals(listOf(job.key), coordinator.requestedOpenKeys)
        assertEquals(
            listOf<DayStudyBackgroundGenerationUiAction>(
                DayStudyBackgroundGenerationUiAction.NavigateToRoute(job.dayRoute),
            ),
            actions,
        )
    }

    @Test
    fun `WHEN dismiss is clicked THEN dismisses each key from the card`() = runTest(testDispatcher) {
        // Given
        val coordinator = FakeDayStudyGenerationCoordinator()
        val viewModel = viewModel(coordinator)

        // When
        viewModel.onEvent(DayStudyBackgroundGenerationUiEvent.OnDismissClick(listOf("key-a", "key-b")))

        // Then
        assertEquals(listOf("key-a", "key-b"), coordinator.dismissedFromCardKeys)
    }

    private fun viewModel(coordinator: DayStudyGenerationCoordinator): DayStudyBackgroundGenerationViewModel =
        DayStudyBackgroundGenerationViewModel(
            generationCoordinator = coordinator,
            trackEvent = { _, _ -> },
        )

    private fun generatingJob(route: DayNavRoute): DayStudyGenerationJob = DayStudyGenerationJob(
        key = "${route.readingPlanType}|${route.weekNumber}|${route.dayNumber}",
        label = "Gênesis 1",
        dayRoute = route,
        phase = null,
        status = DayStudyGenerationStatus.Generating,
    )

    private val dayRoute = DayNavRoute(dayNumber = 1, weekNumber = 1, readingPlanType = "ONE_YEAR")
}
