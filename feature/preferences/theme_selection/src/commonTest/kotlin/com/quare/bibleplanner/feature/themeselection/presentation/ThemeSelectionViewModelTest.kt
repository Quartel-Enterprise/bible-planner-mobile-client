package com.quare.bibleplanner.feature.themeselection.presentation

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.MaterialYouBottomSheetNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.feature.themeselection.presentation.factory.ThemeSelectionUiStateFactory
import com.quare.bibleplanner.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
internal class ThemeSelectionViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ThemeSelectionViewModel
    private lateinit var repository: FakeThemeSelectionRepository
    private lateinit var dynamicColorsEnabled: MutableStateFlow<Boolean>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the stored preferences WHEN observing the state THEN reflects them`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(listOf(Theme.SYSTEM), state.options.filter { it.isActive }.map { it.preference })
        assertEquals(ContrastType.Standard, state.selectedContrast)
        assertEquals(false, state.isMaterialYouToggleOn)
        assertTrue(state.isLoggedIn)
    }

    @Test
    fun `GIVEN the system theme WHEN selecting the dark theme THEN stores it and tracks the change`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ThemeSelectionUiEvent.OnThemeSelected(Theme.DARK))

            // Then
            assertEquals(Theme.DARK, repository.theme.value)
            assertEquals(
                listOf(Theme.DARK),
                viewModel.uiState.value.options
                    .filter { it.isActive }
                    .map { it.preference },
            )
            assertEquals(
                AnalyticsEventNames.THEME_CHANGED to mapOf<String, Any>(AnalyticsParams.THEME to "dark"),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN the standard contrast WHEN selecting the high contrast THEN stores it and tracks the change`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ThemeSelectionUiEvent.OnContrastSelected(ContrastType.High))

            // Then
            assertEquals(ContrastType.High, viewModel.uiState.value.selectedContrast)
            assertEquals(
                AnalyticsEventNames.CONTRAST_CHANGED to mapOf<String, Any>(AnalyticsParams.CONTRAST to "high"),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN dynamic colors off WHEN turning them on THEN enables them`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ThemeSelectionUiEvent.MaterialYouToggleClicked(isNewValueOn = true))

        // Then
        assertEquals(true, viewModel.uiState.value.isMaterialYouToggleOn)
        assertEquals(
            AnalyticsEventNames.DYNAMIC_COLORS_TOGGLED to mapOf<String, Any>(
                AnalyticsParams.IS_ENABLED to true,
                AnalyticsParams.SOURCE to "theme_selection",
            ),
            trackedEvents.single(),
        )
    }

    @Test
    fun `GIVEN sync off WHEN turning it on THEN enables the theme sync`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ThemeSelectionUiEvent.SyncToggleClicked(isNewValueOn = true))

        // Then
        assertTrue(viewModel.uiState.value.isSyncEnabled)
        assertEquals(
            AnalyticsEventNames.SETTING_SYNC_TOGGLED to mapOf<String, Any>(
                AnalyticsParams.SETTING to "theme",
                AnalyticsParams.IS_ENABLED to true,
            ),
            trackedEvents.single(),
        )
    }

    @Test
    fun `GIVEN the sync toggle is blocked WHEN clicking it THEN asks to log in`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ThemeSelectionUiEvent.SyncToggleBlockedClicked)

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.Preferences.Theme.key)),
            ),
            commands,
        )
        assertEquals(AnalyticsEventNames.SYNC_TOGGLE_BLOCKED_CLICKED, trackedEvents.single().first)
    }

    @Test
    fun `GIVEN the theme selection WHEN clicking the material you info THEN opens the material you sheet`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ThemeSelectionUiEvent.MaterialYouInfoClicked)

            // Then
            assertEquals(
                listOf<NavigationCommand>(NavigationCommand.Navigate(MaterialYouBottomSheetNavRoute)),
                commands,
            )
            assertEquals(AnalyticsEventNames.MATERIAL_YOU_INFO_CLICKED to emptyMap(), trackedEvents.single())
        }

    @Test
    fun `GIVEN the theme selection WHEN dismissing it THEN navigates back`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ThemeSelectionUiEvent.OnDismiss)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(AnalyticsEventNames.THEME_SELECTION_DISMISSED to emptyMap(), trackedEvents.single())
    }

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        val recordedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = recordedEvents
        dynamicColorsEnabled = MutableStateFlow(false)
        repository = FakeThemeSelectionRepository(
            initialTheme = Theme.SYSTEM,
            initialContrast = ContrastType.Standard,
            initialSyncEnabled = false,
        )
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        viewModel = ThemeSelectionViewModel(
            setThemeOption = repository::setTheme,
            setDynamicColorsEnabledFlow = { isEnabled -> dynamicColorsEnabled.value = isEnabled },
            setContrastType = repository::setContrastType,
            setThemeSyncEnabled = repository::setThemeSyncEnabled,
            navigator = navigator,
            trackEvent = { name, params -> recordedEvents += name to params },
            factory = ThemeSelectionUiStateFactory(
                getThemeOptionFlow = repository::getThemeFlow,
                getIsDynamicColorsEnabledFlow = { dynamicColorsEnabled },
                getContrastTypeFlow = repository::getContrastTypeFlow,
                isDynamicColorSupported = { true },
                getThemeSyncEnabledFlow = repository::getThemeSyncEnabledFlow,
                observeAuthenticatedUserId = { flowOf("user-1") },
            ),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
    }
}
