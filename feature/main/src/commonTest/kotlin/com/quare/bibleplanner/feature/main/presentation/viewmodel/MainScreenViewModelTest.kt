package com.quare.bibleplanner.feature.main.presentation.viewmodel

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiAction
import com.quare.bibleplanner.feature.main.presentation.model.MainScreenUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
internal class MainScreenViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val profile = UserProfile(
        userId = "user-1",
        displayName = "Pierre",
        email = "pierre@example.com",
        avatar = AvatarSource.Remote(url = "https://example.com/avatar.png"),
        hasVisiblePhoto = true,
        hasProviderPhoto = false,
        isUsingProviderPhoto = false,
        provider = null,
    )
    private lateinit var viewModel: MainScreenViewModel
    private lateinit var profileFlow: MutableStateFlow<UserProfile?>
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var actions: List<MainScreenUiAction>
    private lateinit var navigationItems: List<List<MainNavigationItemModel<NavKey>>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN no subscriber WHEN reading the language THEN starts as english`() = runTest(testDispatcher) {
        // Given
        prepareScenario(language = Language.SPANISH)

        // When
        val language = viewModel.languageState.value

        // Then
        assertEquals(Language.ENGLISH, language)
    }

    @Test
    fun `GIVEN a stored app language WHEN collecting the language THEN exposes it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(language = Language.PORTUGUESE_BRAZIL)

        // When
        backgroundScope.launch { viewModel.languageState.collect {} }

        // Then
        assertEquals(Language.PORTUGUESE_BRAZIL, viewModel.languageState.value)
    }

    @Test
    fun `GIVEN a signed out user WHEN collecting the tabs THEN lists plans books and profile without an avatar`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(profile = null)

            // When
            val items = navigationItems.last()

            // Then
            assertEquals(
                listOf(MainNavRouteDestination.Plans, MainNavRouteDestination.Books, MainNavRouteDestination.Profile),
                items.map(MainNavigationItemModel<NavKey>::route),
            )
            assertEquals(
                MainNavigationIcon.Profile(
                    avatar = AvatarSource.None,
                    displayName = null,
                ),
                items.last().presentationModel.icon,
            )
        }

    @Test
    fun `GIVEN a signed out user WHEN collecting the navigation items THEN plans and books use vector icons`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(profile = null)

            // When
            val items = navigationItems.last()

            // Then
            assertIs<MainNavigationIcon.Vector>(items[0].presentationModel.icon)
            assertIs<MainNavigationIcon.Vector>(items[1].presentationModel.icon)
            assertEquals("book", items[1].presentationModel.iosIcon.symbolName)
            assertEquals("book.fill", items[1].presentationModel.iosIcon.selectedSymbolName)
        }

    @Test
    fun `GIVEN a signed in user WHEN the profile arrives THEN the profile tab shows the avatar and name`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(profile = null)

            // When
            profileFlow.value = profile

            // Then
            assertEquals(
                MainNavigationIcon.Profile(
                    avatar = profile.avatar,
                    displayName = "Pierre",
                ),
                navigationItems
                    .last()
                    .last()
                    .presentationModel.icon,
            )
        }

    @Test
    fun `GIVEN a tab route WHEN clicking the bottom item THEN navigates to it and tracks the tab`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(MainScreenUiEvent.BottomNavItemClicked(MainNavRouteDestination.Books))

            // Then
            assertEquals(listOf(MainScreenUiAction.NavigateToBottomRoute(MainNavRouteDestination.Books)), actions)
            assertEquals(
                listOf(AnalyticsEventNames.BOTTOM_TAB_CLICKED to mapOf<String, Any>(AnalyticsParams.TAB to "books")),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN each main tab WHEN clicking it THEN tracks its tab name`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(MainScreenUiEvent.BottomNavItemClicked(MainNavRouteDestination.Plans))
        viewModel.onEvent(MainScreenUiEvent.BottomNavItemClicked(MainNavRouteDestination.Profile))

        // Then
        assertEquals(
            listOf("plans", "profile"),
            trackedEvents.map { (_, params) -> params.getValue(AnalyticsParams.TAB) },
        )
    }

    @Test
    fun `GIVEN a route outside the tabs WHEN clicking it THEN tracks an unknown tab`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(MainScreenUiEvent.BottomNavItemClicked(ThemeNavRoute))

        // Then
        assertEquals("unknown", trackedEvents.single().second.getValue(AnalyticsParams.TAB))
        assertEquals(listOf(MainScreenUiAction.NavigateToBottomRoute(ThemeNavRoute)), actions)
    }

    private fun TestScope.prepareScenario(
        language: Language = Language.ENGLISH,
        profile: UserProfile? = null,
    ) {
        profileFlow = MutableStateFlow(profile)
        trackedEvents = mutableListOf()
        viewModel = MainScreenViewModel(
            getAppLanguageFlow = { MutableStateFlow(language) },
            observeUserProfile = { profileFlow },
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        actions = mutableListOf<MainScreenUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        navigationItems = mutableListOf<List<MainNavigationItemModel<NavKey>>>().also { collected ->
            backgroundScope.launch { viewModel.mainNavigationItemModels.collect { collected += it } }
        }
    }
}
