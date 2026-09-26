package com.quare.bibleplanner.feature.profile.presentation.factory

import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.contrast_high
import bibleplanner.feature.profile.generated.resources.contrast_medium
import bibleplanner.feature.profile.generated.resources.contrast_standard
import bibleplanner.feature.profile.generated.resources.dynamic_colors
import bibleplanner.feature.profile.generated.resources.pro_and_support
import bibleplanner.feature.profile.generated.resources.pro_section
import bibleplanner.feature.profile.generated.resources.theme_dark
import bibleplanner.feature.profile.generated.resources.theme_light
import bibleplanner.feature.profile.generated.resources.theme_system
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleFlowUseCase
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlanStartDateFlowUseCase
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.profile.domain.model.AccountStatusModel
import com.quare.bibleplanner.feature.profile.domain.usecase.GetSelectedVersionDownloadedChaptersFlowUseCase
import com.quare.bibleplanner.feature.profile.domain.usecase.ObserveShowDonateOptionUseCase
import com.quare.bibleplanner.feature.profile.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.profile.fake.FakeBibleVersionDao
import com.quare.bibleplanner.feature.profile.fake.FakeObserveBooleanRemoteConfig
import com.quare.bibleplanner.feature.profile.fake.FakePlanRepository
import com.quare.bibleplanner.feature.profile.generated.ProfileBuildKonfig
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileUiState
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.model.theme.ContrastType
import com.quare.bibleplanner.core.model.theme.Theme
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ProfileUiStateFactoryTest {
    private val selectedVersion = VersionModel(
        id = "NVI",
        name = "Nova Versão Internacional",
        version = "1",
        language = Language.PORTUGUESE_BRAZIL,
        chapters = 1189,
        size = null,
    )
    private val userProfile = UserProfile(
        userId = "user-1",
        displayName = "Test User",
        email = "user@test.com",
        avatar = AvatarSource.None,
        hasVisiblePhoto = false,
        hasProviderPhoto = false,
        isUsingProviderPhoto = false,
        provider = null,
    )
    private val studySuggestion = StudySuggestionSettingsModel(
        isEnabled = true,
        mode = StudySuggestionMode.DIALOG,
    )
    private val planStartDate = LocalDate(
        year = 2026,
        month = 1,
        day = 1,
    )

    private lateinit var factory: ProfileUiStateFactory
    private lateinit var states: List<ProfileUiState>

    @Test
    fun `GIVEN a desktop platform WHEN creating the initial state THEN hides the update row`() = runTest {
        // Given
        prepareScenario(platform = Platform.Desktop.MacOs)

        // When
        val state = factory.createInitialState()

        // Then
        assertFalse(state.isUpdateRowVisible)
    }

    @Test
    fun `GIVEN a mobile platform WHEN creating the initial state THEN everything loads with the update row visible`() =
        runTest {
            // Given
            prepareScenario(platform = Platform.Android)

            // When
            val state = factory.createInitialState()

            // Then
            assertTrue(state.isUpdateRowVisible)
            assertFalse(state.isCheckingForUpdate)
            assertEquals(
                expected = AccountStatusModel.Loading,
                actual = state.accountStatusModel,
            )
            assertEquals(
                expected = Loadable.Loading,
                actual = state.themeRes,
            )
            assertEquals(
                expected = Loadable.Loaded(true),
                actual = state.isProCardVisible,
            )
            assertEquals(
                expected = ProfileBuildKonfig.APP_VERSION,
                actual = state.appVersion,
            )
        }

    @Test
    fun `GIVEN every source emitted WHEN observing THEN loads every section`() = runTest {
        // Given
        prepareScenario(
            isInstagramVisible = true,
            isWebAppEnabled = true,
            subscriptionStatus = SubscriptionStatus.Free,
        )

        // When
        runCurrent()

        // Then
        val state = states.last()
        assertEquals(
            expected = Loadable.Loaded(true),
            actual = state.isInstagramLinkVisible,
        )
        assertEquals(
            expected = Loadable.Loaded(true),
            actual = state.isWebAppVisible,
        )
        assertEquals(
            expected = Loadable.Loaded(false),
            actual = state.shouldShowDonateOption,
        )
        assertEquals(
            expected = Loadable.Loaded(Language.ENGLISH),
            actual = state.selectedLanguage,
        )
        assertEquals(
            expected = Loadable.Loaded(planStartDate),
            actual = state.planStartDate,
        )
        assertEquals(
            expected = Loadable.Loaded(studySuggestion),
            actual = state.studySuggestion,
        )
        assertEquals(
            expected = Loadable.Loaded(SubscriptionStatus.Free),
            actual = state.subscriptionStatus,
        )
        assertEquals(
            expected = Loadable.Loaded(selectedVersion.name),
            actual = state.bibleVersionName,
        )
    }

    @Test
    fun `GIVEN the donate option is shown WHEN observing THEN titles the header as pro and support`() = runTest {
        // Given
        prepareScenario(shouldShowDonate = true)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(true),
            actual = states.last().shouldShowDonateOption,
        )
        assertEquals(
            expected = Loadable.Loaded(Res.string.pro_and_support),
            actual = states.last().headerRes,
        )
    }

    @Test
    fun `GIVEN the donate option is hidden WHEN observing THEN titles the header as pro only`() = runTest {
        // Given
        prepareScenario(shouldShowDonate = false)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(Res.string.pro_section),
            actual = states.last().headerRes,
        )
    }

    @Test
    fun `GIVEN the light theme WHEN observing THEN shows the light theme label`() = runTest {
        // Given
        prepareScenario(theme = Theme.LIGHT)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(Res.string.theme_light),
            actual = states.last().themeRes,
        )
    }

    @Test
    fun `GIVEN the dark theme WHEN observing THEN shows the dark theme label`() = runTest {
        // Given
        prepareScenario(theme = Theme.DARK)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(Res.string.theme_dark),
            actual = states.last().themeRes,
        )
    }

    @Test
    fun `GIVEN the system theme WHEN observing THEN shows the system theme label`() = runTest {
        // Given
        prepareScenario(theme = Theme.SYSTEM)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(Res.string.theme_system),
            actual = states.last().themeRes,
        )
    }

    @Test
    fun `GIVEN standard contrast without dynamic colors WHEN observing THEN shows the standard contrast label`() =
        runTest {
            // Given
            prepareScenario(contrast = ContrastType.Standard)

            // When
            runCurrent()

            // Then
            assertEquals(
                expected = Loadable.Loaded(Res.string.contrast_standard),
                actual = states.last().contrastRes,
            )
        }

    @Test
    fun `GIVEN medium contrast without dynamic colors WHEN observing THEN shows the medium contrast label`() = runTest {
        // Given
        prepareScenario(contrast = ContrastType.Medium)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(Res.string.contrast_medium),
            actual = states.last().contrastRes,
        )
    }

    @Test
    fun `GIVEN high contrast without dynamic colors WHEN observing THEN shows the high contrast label`() = runTest {
        // Given
        prepareScenario(contrast = ContrastType.High)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(Res.string.contrast_high),
            actual = states.last().contrastRes,
        )
    }

    @Test
    fun `GIVEN dynamic colors enabled and supported WHEN observing THEN shows the dynamic colors label`() = runTest {
        // Given
        prepareScenario(
            isDynamicColorsEnabled = true,
            isDynamicColorSupported = true,
        )

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(Res.string.dynamic_colors),
            actual = states.last().contrastRes,
        )
    }

    @Test
    fun `GIVEN dynamic colors enabled but unsupported WHEN observing THEN falls back to the contrast label`() =
        runTest {
            // Given
            prepareScenario(
                contrast = ContrastType.High,
                isDynamicColorsEnabled = true,
                isDynamicColorSupported = false,
            )

            // When
            runCurrent()

            // Then
            assertEquals(
                expected = Loadable.Loaded(Res.string.contrast_high),
                actual = states.last().contrastRes,
            )
        }

    @Test
    fun `GIVEN no billing on the platform WHEN observing THEN loads a null subscription`() = runTest {
        // Given
        prepareScenario(hasBilling = false)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(null),
            actual = states.last().subscriptionStatus,
        )
    }

    @Test
    fun `GIVEN an authenticated session with a profile WHEN observing THEN the account is logged in`() = runTest {
        // Given
        prepareScenario(sessionStatus = authenticated())

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = AccountStatusModel.LoggedIn(userProfile),
            actual = states.last().accountStatusModel,
        )
    }

    @Test
    fun `GIVEN an authenticated session without a profile WHEN observing THEN the account is in error`() = runTest {
        // Given
        prepareScenario(
            sessionStatus = authenticated(),
            profile = null,
        )

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = AccountStatusModel.Error,
            actual = states.last().accountStatusModel,
        )
    }

    @Test
    fun `GIVEN an initializing session WHEN observing THEN the account is loading`() = runTest {
        // Given
        prepareScenario(sessionStatus = SessionStatus.Initializing)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = AccountStatusModel.Loading,
            actual = states.last().accountStatusModel,
        )
    }

    @Test
    fun `GIVEN no session WHEN observing THEN the account is logged out`() = runTest {
        // Given
        prepareScenario(sessionStatus = SessionStatus.NotAuthenticated())

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = AccountStatusModel.LoggedOut,
            actual = states.last().accountStatusModel,
        )
    }

    @Test
    fun `GIVEN a failed session refresh WHEN observing THEN the account is in error`() = runTest {
        // Given
        prepareScenario(
            sessionStatus = SessionStatus.RefreshFailure(RefreshFailureCause.NetworkError(Exception("offline"))),
        )

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = AccountStatusModel.Error,
            actual = states.last().accountStatusModel,
        )
    }

    @Test
    fun `GIVEN the selected version is downloading WHEN observing THEN shows the downloaded fraction`() = runTest {
        // Given
        prepareScenario(
            versionStatus = DownloadStatus.IN_PROGRESS,
            downloadedChapters = 100,
            totalChapters = 400,
        )

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(0.25f),
            actual = states.last().bibleDownloadProgress,
        )
    }

    @Test
    fun `GIVEN the selected version is fully downloaded WHEN observing THEN shows no progress`() = runTest {
        // Given
        prepareScenario(
            versionStatus = DownloadStatus.DONE,
            downloadedChapters = 400,
            totalChapters = 400,
        )

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = Loadable.Loaded(null),
            actual = states.last().bibleDownloadProgress,
        )
    }

    @Test
    fun `GIVEN no selected version WHEN observing THEN shows no version name nor progress`() = runTest {
        // Given
        prepareScenario(isVersionSelected = false)

        // When
        runCurrent()

        // Then
        val state = states.last()
        assertEquals(
            expected = Loadable.Loaded(null),
            actual = state.bibleVersionName,
        )
        assertNull((state.bibleDownloadProgress as Loadable.Loaded).value)
    }

    private fun authenticated() = SessionStatus.Authenticated(
        session = UserSession(
            accessToken = "",
            refreshToken = "",
            expiresIn = 0,
            tokenType = "",
            user = UserInfo(
                aud = "",
                id = "user-1",
            ),
        ),
    )

    private fun TestScope.prepareScenario(
        platform: Platform = Platform.Android,
        isInstagramVisible: Boolean = false,
        shouldShowDonate: Boolean = false,
        isWebAppEnabled: Boolean = false,
        theme: Theme = Theme.SYSTEM,
        contrast: ContrastType = ContrastType.Standard,
        isDynamicColorsEnabled: Boolean = false,
        isDynamicColorSupported: Boolean = false,
        hasBilling: Boolean = true,
        subscriptionStatus: SubscriptionStatus? = null,
        sessionStatus: SessionStatus = SessionStatus.NotAuthenticated(),
        profile: UserProfile? = userProfile,
        isVersionSelected: Boolean = true,
        versionStatus: DownloadStatus = DownloadStatus.DONE,
        downloadedChapters: Int = 0,
        totalChapters: Int = 1189,
    ) {
        val bibleRepository = FakeBibleRepository(
            bibles = listOf(
                BibleModel(
                    version = selectedVersion,
                    downloadedChapters = downloadedChapters,
                    downloadStatus = DownloadStatusModel.NotStarted,
                    isSelected = isVersionSelected,
                    hasPendingUpdate = false,
                ),
            ),
        )
        val getSelectedBible = GetSelectedBibleFlowUseCase(bibleRepository)
        factory = ProfileUiStateFactory(
            getSubscriptionStatusFlow = if (hasBilling) {
                { flowOf(subscriptionStatus) }
            } else {
                null
            },
            isInstagramLinkVisible = { flowOf(isInstagramVisible) },
            shouldShowDonateOption = ObserveShowDonateOptionUseCase(FakeObserveBooleanRemoteConfig(shouldShowDonate)),
            getPlanStartDate = GetPlanStartDateFlowUseCase(FakePlanRepository(planStartDate)),
            getThemeOptionFlow = { flowOf(theme) },
            getContrastTypeFlow = { flowOf(contrast) },
            getIsDynamicColorsEnabledFlow = { flowOf(isDynamicColorsEnabled) },
            isProfileWebAppEnabled = { flowOf(isWebAppEnabled) },
            isDynamicColorSupported = { isDynamicColorSupported },
            sessionStatus = MutableStateFlow(sessionStatus),
            observeUserProfile = { flowOf(profile) },
            bibleVersionDao = FakeBibleVersionDao(
                versions = listOf(
                    BibleVersionEntity(
                        id = selectedVersion.id,
                        status = versionStatus,
                        totalChapters = totalChapters,
                        contentVersion = "",
                    ),
                ),
            ),
            getSelectedVersionDownloadedChapters = GetSelectedVersionDownloadedChaptersFlowUseCase(getSelectedBible),
            getSelectedBible = getSelectedBible,
            getAppLanguageFlow = { flowOf(Language.ENGLISH) },
            observeStudySuggestionSettings = { flowOf(studySuggestion) },
            platform = platform,
        )
        states = mutableListOf<ProfileUiState>().also { collected ->
            backgroundScope.launch {
                factory.create(initialState = factory.createInitialState()).collect { collected += it }
            }
        }
    }
}
