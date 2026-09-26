package com.quare.bibleplanner.feature.congrats.presentation.viewmodel

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.feature.congrats.presentation.model.CongratsUiEvent
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class CongratsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: CongratsViewModel
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var trackedEvents: MutableList<String>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the congrats sheet WHEN dismissing it THEN closes it and tracks the dismissal`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(CongratsUiEvent.OnDismiss)

            // Then
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(listOf(AnalyticsEventNames.CONGRATS_DISMISSED), trackedEvents)
        }

    @Test
    fun `GIVEN the congrats sheet WHEN starting to explore THEN closes it and tracks the click`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(CongratsUiEvent.OnStartExploring)

            // Then
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(listOf(AnalyticsEventNames.CONGRATS_EXPLORE_CLICKED), trackedEvents)
        }

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        commands = mutableListOf()
        trackedEvents = mutableListOf()
        backgroundScope.launch { navigator.commands.collect(commands::add) }
        viewModel = CongratsViewModel(
            navigator = navigator,
            trackEvent = { name, _ -> trackedEvents += name },
        )
    }
}
