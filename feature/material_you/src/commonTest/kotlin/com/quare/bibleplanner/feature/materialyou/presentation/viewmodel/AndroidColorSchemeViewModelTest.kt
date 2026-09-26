package com.quare.bibleplanner.feature.materialyou.presentation.viewmodel

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.feature.materialyou.domain.model.MaterialYouUseCases
import com.quare.bibleplanner.feature.materialyou.fake.FakeMaterialYouRepository
import com.quare.bibleplanner.feature.materialyou.presentation.model.AndroidColorSchemeUiEvent
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class AndroidColorSchemeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: AndroidColorSchemeViewModel
    private lateinit var repository: FakeMaterialYouRepository
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
    fun `GIVEN dynamic colors stored as enabled WHEN the screen opens THEN shows them enabled`() =
        runTest(testDispatcher) {
            // When
            prepareScenario(isDynamicColorsEnabled = true)

            // Then
            assertTrue(viewModel.uiState.value)
        }

    @Test
    fun `GIVEN dynamic colors disabled WHEN toggling them on THEN stores and shows them enabled`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isDynamicColorsEnabled = false)

            // When
            viewModel.onEvent(AndroidColorSchemeUiEvent.OnIsDynamicColorsEnabledChange(isEnabled = true))

            // Then
            assertEquals(listOf(true), repository.writes)
            assertTrue(viewModel.uiState.value)
            assertEquals(listOf(AnalyticsEventNames.DYNAMIC_COLORS_TOGGLED), trackedEvents)
        }

    @Test
    fun `GIVEN the information dialog WHEN dismissing it THEN navigates back`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isDynamicColorsEnabled = false)

        // When
        viewModel.onEvent(AndroidColorSchemeUiEvent.OnInformationDialogDismiss)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertFalse(viewModel.uiState.value)
    }

    @Test
    fun `GIVEN the information sheet WHEN tapping got it THEN navigates back`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isDynamicColorsEnabled = false)

        // When
        viewModel.onEvent(AndroidColorSchemeUiEvent.BottomSheetGotItClick)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(listOf(AnalyticsEventNames.MATERIAL_YOU_GOT_IT_CLICKED), trackedEvents)
    }

    private fun TestScope.prepareScenario(isDynamicColorsEnabled: Boolean) {
        val navigator = Navigator()
        repository = FakeMaterialYouRepository(isDynamicColorsEnabled)
        commands = mutableListOf()
        trackedEvents = mutableListOf()
        backgroundScope.launch { navigator.commands.collect(commands::add) }
        viewModel = AndroidColorSchemeViewModel(
            useCases = MaterialYouUseCases(
                getIsDynamicColorsEnabledFlow = repository::getIsDynamicColorsEnabledFlow,
                setIsDynamicColorsEnabled = repository::setIsDynamicColorsEnabled,
            ),
            navigator = navigator,
            trackEvent = { name, _ -> trackedEvents += name },
        )
    }
}
