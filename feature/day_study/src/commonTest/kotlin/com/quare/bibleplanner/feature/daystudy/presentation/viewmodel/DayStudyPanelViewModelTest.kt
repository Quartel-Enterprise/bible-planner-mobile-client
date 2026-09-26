package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPanelRatio
import com.quare.bibleplanner.feature.daystudy.domain.usecase.FakeDayStudyPanelRatioRepository
import com.quare.bibleplanner.feature.daystudy.domain.usecase.ObserveDayStudyPanelReadingFractionUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.SetDayStudyPanelReadingFractionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class DayStudyPanelViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: DayStudyPanelViewModel
    private lateinit var repository: FakeDayStudyPanelRatioRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN no stored fraction WHEN observing THEN exposes the default split`() = runTest(testDispatcher) {
        // Given
        prepareScenario(storedFraction = null)

        // When
        val fraction = viewModel.readingFraction.value

        // Then
        assertEquals(DayStudyPanelRatio.DEFAULT, fraction)
    }

    @Test
    fun `GIVEN a stored fraction WHEN observing THEN exposes it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(storedFraction = 0.5f)

        // When
        val fraction = viewModel.readingFraction.value

        // Then
        assertEquals(0.5f, fraction)
    }

    @Test
    fun `GIVEN a drag beyond the limits WHEN changing the fraction THEN stores and exposes the clamped value`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(storedFraction = null)

            // When
            viewModel.onReadingFractionChanged(0.9f)

            // Then
            assertEquals(DayStudyPanelRatio.MAX, repository.lastSetFraction)
            assertEquals(DayStudyPanelRatio.MAX, viewModel.readingFraction.value)
        }

    private fun prepareScenario(storedFraction: Float?) {
        repository = FakeDayStudyPanelRatioRepository(storedFraction)
        viewModel = DayStudyPanelViewModel(
            setReadingFraction = SetDayStudyPanelReadingFractionUseCase(repository),
            observeReadingFraction = ObserveDayStudyPanelReadingFractionUseCase(repository),
        )
    }
}
