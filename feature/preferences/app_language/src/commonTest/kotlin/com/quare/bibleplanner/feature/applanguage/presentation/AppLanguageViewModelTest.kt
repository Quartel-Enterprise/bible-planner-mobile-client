package com.quare.bibleplanner.feature.applanguage.presentation

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.GetLanguageSyncEnabledFlowUseCase
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.SetAppLanguageUseCase
import com.quare.bibleplanner.feature.applanguage.domain.usecase.impl.SetLanguageSyncEnabledUseCase
import com.quare.bibleplanner.feature.applanguage.presentation.factory.AppLanguageUiStateFactory
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiAction
import com.quare.bibleplanner.feature.applanguage.presentation.model.AppLanguageUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
internal class AppLanguageViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: AppLanguageViewModel
    private lateinit var repository: FakeAppLanguageRepository
    private lateinit var actions: List<AppLanguageUiAction>
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
    fun `GIVEN english WHEN selecting spanish THEN stores it applies it and closes the picker`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(AppLanguageUiEvent.OnLanguageSelected(Language.SPANISH))

            // Then
            assertEquals(Language.SPANISH, repository.language.value)
            assertEquals(Language.SPANISH, viewModel.uiState.value.selectedLanguage)
            assertEquals(listOf<AppLanguageUiAction>(AppLanguageUiAction.ApplyLanguage(Language.SPANISH)), actions)
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(
                AnalyticsEventNames.LANGUAGE_CHANGED to mapOf<String, Any>(AnalyticsParams.LANGUAGE to "es"),
                trackedEvents.single(),
            )
        }

    @Test
    fun `GIVEN english WHEN selecting brazilian portuguese THEN tracks it as portuguese`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(AppLanguageUiEvent.OnLanguageSelected(Language.PORTUGUESE_BRAZIL))

        // Then
        assertEquals(
            AnalyticsEventNames.LANGUAGE_CHANGED to mapOf<String, Any>(AnalyticsParams.LANGUAGE to "pt"),
            trackedEvents.single(),
        )
    }

    @Test
    fun `GIVEN the language picker WHEN selecting english THEN tracks it as english`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(AppLanguageUiEvent.OnLanguageSelected(Language.ENGLISH))

        // Then
        assertEquals(
            AnalyticsEventNames.LANGUAGE_CHANGED to mapOf<String, Any>(AnalyticsParams.LANGUAGE to "en"),
            trackedEvents.single(),
        )
    }

    @Test
    fun `GIVEN sync off WHEN turning it on THEN enables the language sync`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(AppLanguageUiEvent.SyncToggleClicked(isNewValueOn = true))

        // Then
        assertTrue(viewModel.uiState.value.isSyncEnabled)
        assertEquals(
            AnalyticsEventNames.SETTING_SYNC_TOGGLED to mapOf<String, Any>(
                AnalyticsParams.SETTING to "language",
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
        viewModel.onEvent(AppLanguageUiEvent.SyncToggleBlockedClicked)

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.Preferences.Language.key)),
            ),
            commands,
        )
        assertEquals(
            AnalyticsEventNames.SYNC_TOGGLE_BLOCKED_CLICKED to
                mapOf<String, Any>(AnalyticsParams.SOURCE to "app_language"),
            trackedEvents.single(),
        )
    }

    @Test
    fun `GIVEN the language picker WHEN dismissing it THEN navigates back`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(AppLanguageUiEvent.OnDismiss)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(AnalyticsEventNames.APP_LANGUAGE_DISMISSED to emptyMap(), trackedEvents.single())
    }

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        val recordedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = recordedEvents
        repository = FakeAppLanguageRepository(
            initialLanguage = Language.ENGLISH,
            initialSyncEnabled = false,
        )
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        viewModel = AppLanguageViewModel(
            setAppLanguage = SetAppLanguageUseCase(repository),
            setLanguageSyncEnabled = SetLanguageSyncEnabledUseCase(repository),
            navigator = navigator,
            trackEvent = { name, params -> recordedEvents += name to params },
            factory = AppLanguageUiStateFactory(
                getAppLanguageFlow = repository::getLanguageFlow,
                getLanguageSyncEnabledFlow = GetLanguageSyncEnabledFlowUseCase(repository),
                observeAuthenticatedUserId = { flowOf("user-1") },
                languageProvider = EnglishLanguageProvider(),
            ),
        )
        actions = mutableListOf<AppLanguageUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        backgroundScope.launch { viewModel.uiState.collect {} }
    }
}

private class EnglishLanguageProvider : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("unused")

    override fun getAppLanguage(): Language = Language.ENGLISH
}
