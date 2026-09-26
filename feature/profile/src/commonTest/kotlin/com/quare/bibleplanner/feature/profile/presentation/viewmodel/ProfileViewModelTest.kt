package com.quare.bibleplanner.feature.profile.presentation.viewmodel

import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.delete_account_requires_internet
import bibleplanner.feature.profile.generated.resources.login_requires_internet
import bibleplanner.feature.profile.generated.resources.logout_requires_internet
import bibleplanner.feature.profile.generated.resources.up_to_date_message
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleFlowUseCase
import com.quare.bibleplanner.core.inappupdate.domain.UpdatePromptSource
import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.model.legal.LegalUrl
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.route.AccountDetailsNavRoute
import com.quare.bibleplanner.core.model.route.AppLanguageNavRoute
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.ContactSupportNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAccountNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.model.route.EditProfileNavRoute
import com.quare.bibleplanner.core.model.route.ExpandedPhotoNavRoute
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.model.route.StudySuggestionNavRoute
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.domain.usecase.GetAppStoreLinkUseCase
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.profile.domain.usecase.GetInstagramUrlUseCase
import com.quare.bibleplanner.feature.profile.domain.usecase.GetSelectedVersionDownloadedChaptersFlowUseCase
import com.quare.bibleplanner.feature.profile.domain.usecase.ObserveShowDonateOptionUseCase
import com.quare.bibleplanner.feature.profile.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.profile.fake.FakeBibleVersionDao
import com.quare.bibleplanner.feature.profile.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.profile.fake.FakeLanguageProvider
import com.quare.bibleplanner.feature.profile.fake.FakeObserveBooleanRemoteConfig
import com.quare.bibleplanner.feature.profile.fake.FakePlanRepository
import com.quare.bibleplanner.feature.profile.presentation.factory.ProfileUiStateFactory
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiAction
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiEvent
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ProfileViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val webAppUrl = "https://web.bibleplanner.app"
    private lateinit var viewModel: ProfileViewModel
    private lateinit var navigator: Navigator
    private lateinit var commands: List<NavigationCommand>
    private lateinit var actions: List<ProfileUiAction>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>
    private lateinit var shownPrompts: List<Pair<UpdateAvailability.Available, String>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the profile WHEN tapping a settings row THEN navigates to its screen`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val expectedRoutes = listOf(
            ProfileOptionItemType.THEME to ThemeNavRoute,
            ProfileOptionItemType.APP_LANGUAGE to AppLanguageNavRoute,
            ProfileOptionItemType.STUDY_SUGGESTION to StudySuggestionNavRoute,
            ProfileOptionItemType.BECOME_PRO to PaywallNavRoute(PaywallEntrySource.PROFILE_MENU),
            ProfileOptionItemType.EDIT_PLAN_START_DAY to EditPlanStartDateNavRoute,
            ProfileOptionItemType.DONATE to DonationNavRoute,
            ProfileOptionItemType.RELEASE_NOTES to ReleaseNotesNavRoute,
            ProfileOptionItemType.BIBLE_VERSION to BibleVersionSelectorRoute,
            ProfileOptionItemType.CONTACT_SUPPORT to ContactSupportNavRoute,
        )

        // When
        expectedRoutes.forEach { (type, _) -> viewModel.onEvent(ProfileUiEvent.OnItemClick(type)) }
        runCurrent()

        // Then
        assertEquals(
            expected = expectedRoutes.map { (_, route) -> NavigationCommand.Navigate(route) },
            actual = commands,
        )
    }

    @Test
    fun `GIVEN the profile WHEN tapping the header cards THEN navigates to their screens`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ProfileUiEvent.OnProCardClick)
        viewModel.onEvent(ProfileUiEvent.OnAccountCardClick)
        viewModel.onEvent(ProfileUiEvent.OnEditProfileClick)
        viewModel.onEvent(ProfileUiEvent.OnAvatarClick)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                SubscriptionDetailsNavRoute,
                AccountDetailsNavRoute,
                EditProfileNavRoute,
                ExpandedPhotoNavRoute,
            ).map(NavigationCommand::Navigate),
            actual = commands,
        )
    }

    @Test
    fun `GIVEN the profile WHEN tapping an option THEN tracks the clicked option`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.THEME))

        // Then
        assertEquals(
            expected = listOf("profile_option_clicked" to mapOf<String, Any>("option" to "theme")),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN the profile WHEN tapping the legal rows THEN opens the legal pages`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.PRIVACY_POLICY))
        viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.TERMS))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                ProfileUiAction.OpenLink(LegalUrl.PRIVACY_POLICY),
                ProfileUiAction.OpenLink(LegalUrl.TERMS_OF_SERVICE),
            ),
            actual = actions,
        )
    }

    @Test
    fun `GIVEN a portuguese app WHEN tapping instagram THEN opens the brazilian profile`() = runTest(testDispatcher) {
        // Given
        prepareScenario(appLanguage = Language.PORTUGUESE_BRAZIL)

        // When
        viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.INSTAGRAM))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(ProfileUiAction.OpenLink("https://www.instagram.com/bible.planner.brasil")),
            actual = actions,
        )
    }

    @Test
    fun `GIVEN a spanish app WHEN tapping instagram THEN opens the spanish profile`() = runTest(testDispatcher) {
        // Given
        prepareScenario(appLanguage = Language.SPANISH)

        // When
        viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.INSTAGRAM))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(ProfileUiAction.OpenLink("https://www.instagram.com/bible.planner.espanol")),
            actual = actions,
        )
    }

    @Test
    fun `GIVEN an english app WHEN tapping instagram THEN opens the default profile`() = runTest(testDispatcher) {
        // Given
        prepareScenario(appLanguage = Language.ENGLISH)

        // When
        viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.INSTAGRAM))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(ProfileUiAction.OpenLink("https://www.instagram.com/bible.planner")),
            actual = actions,
        )
    }

    @Test
    fun `GIVEN the profile WHEN tapping the web app row THEN opens the web app url`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.WEB_APP))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(ProfileUiAction.OpenLink(webAppUrl)),
            actual = actions,
        )
    }

    @Test
    fun `GIVEN the desktop app WHEN tapping rate app THEN opens the store link of the platform`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(platform = Platform.Desktop.Linux)

            // When
            viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.RATE_APP))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(ProfileUiAction.OpenLink("https://bibleplanner.app")),
                actual = actions,
            )
        }

    @Test
    fun `GIVEN some reading progress WHEN tapping delete progress THEN opens the delete progress confirmation`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(hasReadingProgress = true)

            // When
            viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.DELETE_PROGRESS))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf<NavigationCommand>(NavigationCommand.Navigate(DeleteAllProgressNavRoute)),
                actual = commands,
            )
            assertTrue(actions.isEmpty())
        }

    @Test
    fun `GIVEN no reading progress WHEN tapping delete progress THEN tells there is nothing to delete`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(hasReadingProgress = false)

            // When
            viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.DELETE_PROGRESS))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf<ProfileUiAction>(ProfileUiAction.ShowNoProgressToDelete),
                actual = actions,
            )
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN a connection WHEN tapping the account rows THEN navigates to their flows`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isConnected = true)

        // When
        viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.DELETE_ACCOUNT))
        viewModel.onEvent(ProfileUiEvent.OnLoginClick)
        viewModel.onEvent(ProfileUiEvent.OnLogoutClick)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                DeleteAccountNavRoute,
                LoginNavRoute(notifyResultViaSnackbar = false),
                LogoutNavRoute,
            ).map(NavigationCommand::Navigate),
            actual = commands,
        )
    }

    @Test
    fun `GIVEN no connection WHEN tapping the account rows THEN explains each one requires internet`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isConnected = false)

            // When
            viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.DELETE_ACCOUNT))
            viewModel.onEvent(ProfileUiEvent.OnLoginClick)
            viewModel.onEvent(ProfileUiEvent.OnLogoutClick)
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(
                    Res.string.delete_account_requires_internet,
                    Res.string.login_requires_internet,
                    Res.string.logout_requires_internet,
                ).map(ProfileUiAction::ShowSnackbar),
                actual = actions,
            )
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN an available update WHEN checking for updates THEN shows the manual update prompt`() =
        runTest(testDispatcher) {
            // Given
            val availability = UpdateAvailability.Available(versionName = "3.0.0")
            prepareScenario(checkForUpdate = { availability })

            // When
            viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.CHECK_FOR_UPDATE))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(availability to UpdatePromptSource.MANUAL),
                actual = shownPrompts,
            )
            assertTrue(actions.isEmpty())
        }

    @Test
    fun `GIVEN the app is up to date WHEN checking for updates THEN says it is up to date`() = runTest(testDispatcher) {
        // Given
        prepareScenario(checkForUpdate = { UpdateAvailability.NotAvailable })

        // When
        viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.CHECK_FOR_UPDATE))
        runCurrent()

        // Then
        assertEquals(
            expected = listOf<ProfileUiAction>(ProfileUiAction.ShowSnackbar(Res.string.up_to_date_message)),
            actual = actions,
        )
        assertTrue(shownPrompts.isEmpty())
    }

    @Test
    fun `GIVEN an update check in flight WHEN observing the state THEN shows the check as running`() =
        runTest(testDispatcher) {
            // Given
            val pendingCheck = CompletableDeferred<UpdateAvailability>()
            prepareScenario(checkForUpdate = pendingCheck::await)
            val states = observeStates()

            // When
            viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.CHECK_FOR_UPDATE))
            runCurrent()

            // Then
            assertTrue(states.last().isCheckingForUpdate)
        }

    @Test
    fun `GIVEN an update check in flight WHEN tapping check for updates again THEN checks only once`() =
        runTest(testDispatcher) {
            // Given
            val pendingCheck = CompletableDeferred<UpdateAvailability>()
            var checks = 0
            prepareScenario(
                checkForUpdate = {
                    checks++
                    pendingCheck.await()
                },
            )
            viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.CHECK_FOR_UPDATE))

            // When
            viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.CHECK_FOR_UPDATE))
            runCurrent()

            // Then
            assertEquals(
                expected = 1,
                actual = checks,
            )
        }

    @Test
    fun `GIVEN a finished update check WHEN observing the state THEN no check is running`() = runTest(testDispatcher) {
        // Given
        val pendingCheck = CompletableDeferred<UpdateAvailability>()
        prepareScenario(checkForUpdate = pendingCheck::await)
        val states = observeStates()
        viewModel.onEvent(ProfileUiEvent.OnItemClick(ProfileOptionItemType.CHECK_FOR_UPDATE))

        // When
        pendingCheck.complete(UpdateAvailability.NotAvailable)
        runCurrent()

        // Then
        assertFalse(states.last().isCheckingForUpdate)
    }

    @Test
    fun `GIVEN the profile sources WHEN observing the state THEN reduces them into the screen state`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            val states = observeStates()

            // Then
            assertEquals(
                expected = Language.ENGLISH,
                actual = states.last().selectedLanguage.valueOrNull(),
            )
        }

    private fun TestScope.observeStates(): List<ProfileUiState> = mutableListOf<ProfileUiState>().also { collected ->
        backgroundScope.launch { viewModel.uiState.collect { collected += it } }
    }

    private fun TestScope.prepareScenario(
        appLanguage: Language = Language.ENGLISH,
        platform: Platform = Platform.Android,
        isConnected: Boolean = true,
        hasReadingProgress: Boolean = false,
        checkForUpdate: suspend () -> UpdateAvailability = { UpdateAvailability.NotAvailable },
    ) {
        val collectedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        val collectedPrompts = mutableListOf<Pair<UpdateAvailability.Available, String>>()
        trackedEvents = collectedEvents
        shownPrompts = collectedPrompts
        navigator = Navigator()
        val languageProvider = FakeLanguageProvider(appLanguage)
        val bibleRepository = FakeBibleRepository(bibles = emptyList())
        val getSelectedBible = GetSelectedBibleFlowUseCase(bibleRepository)
        viewModel = ProfileViewModel(
            calculateBibleProgress = CalculateBibleProgressUseCase(
                FakeBooksRepository(
                    books = listOf(
                        BookDataModel(
                            id = BookId.GEN,
                            chapters = listOf(
                                BookChapterModel(
                                    number = 1,
                                    verses = listOf(
                                        VerseModel(
                                            number = 1,
                                            isRead = hasReadingProgress,
                                        ),
                                        VerseModel(
                                            number = 2,
                                            isRead = false,
                                        ),
                                    ),
                                    isRead = false,
                                    readUpdatedAt = null,
                                ),
                            ),
                            isRead = false,
                            isFavorite = false,
                        ),
                    ),
                ),
            ),
            getWebAppUrl = { webAppUrl },
            getInstagramUrl = GetInstagramUrlUseCase(languageProvider),
            getAppStoreLink = GetAppStoreLinkUseCase(
                platform = platform,
                languageProvider = languageProvider,
            ),
            isConnected = { isConnected },
            checkForUpdate = checkForUpdate,
            showUpdatePrompt = { availability, source -> collectedPrompts += availability to source },
            navigator = navigator,
            uiStateFactory = ProfileUiStateFactory(
                getSubscriptionStatusFlow = null,
                isInstagramLinkVisible = { flowOf(false) },
                shouldShowDonateOption = ObserveShowDonateOptionUseCase(FakeObserveBooleanRemoteConfig(false)),
                getPlanStartDate = GetPlanStartDateFlowUseCase(FakePlanRepository(null)),
                getThemeOptionFlow = { flowOf(Theme.SYSTEM) },
                getContrastTypeFlow = { flowOf(ContrastType.Standard) },
                getIsDynamicColorsEnabledFlow = { flowOf(false) },
                isProfileWebAppEnabled = { flowOf(false) },
                isDynamicColorSupported = { false },
                sessionStatus = MutableStateFlow(SessionStatus.NotAuthenticated()),
                observeUserProfile = { flowOf(null) },
                bibleVersionDao = FakeBibleVersionDao(versions = emptyList()),
                getSelectedVersionDownloadedChapters = GetSelectedVersionDownloadedChaptersFlowUseCase(
                    getSelectedBible,
                ),
                getSelectedBible = getSelectedBible,
                getAppLanguageFlow = { flowOf(appLanguage) },
                observeStudySuggestionSettings = {
                    flowOf(
                        StudySuggestionSettingsModel(
                            isEnabled = true,
                            mode = StudySuggestionMode.DIALOG,
                        ),
                    )
                },
                platform = platform,
            ),
            trackEvent = { name, params -> collectedEvents += name to params },
        )
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        actions = mutableListOf<ProfileUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}
