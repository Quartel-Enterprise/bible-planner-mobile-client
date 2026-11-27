package com.quare.bibleplanner.feature.main.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.main.generated.resources.Res
import bibleplanner.feature.main.generated.resources.books
import bibleplanner.feature.main.generated.resources.plans
import bibleplanner.feature.main.generated.resources.profile
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
import com.quare.bibleplanner.feature.main.presentation.screen.toTabIcon
import com.quare.bibleplanner.feature.main.presentation.screen.toTabTitle
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
    fun `GIVEN a signed out user WHEN collecting the navigation items THEN plans and books use default icons`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(profile = null)

            // When
            val items = navigationItems.last()

            // Then
            assertEquals(MainNavigationIcon.Default, items[0].presentationModel.icon)
            assertEquals(MainNavigationIcon.Default, items[1].presentationModel.icon)
            assertEquals("book", items[1].presentationModel.iosIcon.symbolName)
            assertEquals("book.fill", items[1].presentationModel.iosIcon.selectedSymbolName)
        }

    @Test
    fun `GIVEN the navigation items WHEN the Compose UI labels them THEN each tab gets its own title`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(profile = null)

            // When
            val titles = navigationItems.last().map { item -> item.route.toTabTitle() }

            // Then
            assertEquals(listOf(Res.string.plans, Res.string.books, Res.string.profile), titles)
        }

    @Test
    fun `GIVEN the navigation items WHEN the Compose UI draws their icons THEN each tab gets its own icon`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(profile = null)

            // When
            val icons = navigationItems.last().map { item -> item.route.toTabIcon() }

            // Then
            assertEquals(
                listOf(Icons.Default.DateRange, Icons.AutoMirrored.Filled.MenuBook, Icons.Default.Person),
                icons,
            )
        }

    @Test
    fun `GIVEN the navigation items WHEN reading their test tags THEN they match the benchmark journeys`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(profile = null)

            // When
            val testTags = navigationItems.last().map { item -> item.presentationModel.testTag }

            // Then
            assertEquals(listOf("plans_tab", "books_tab", "profile_tab"), testTags)
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

    @Test
    fun `GIVEN a signed in user WHEN a native UI watches the tabs THEN receives the avatar of the profile tab`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(profile = null)
            val watchedItems = mutableListOf<List<MainNavigationItemModel<NavKey>>>()
            viewModel.navigationItemsWatcher.observe { watchedItems += it }

            // When
            profileFlow.value = profile

            // Then
            assertEquals(
                MainNavigationIcon.Profile(
                    avatar = profile.avatar,
                    displayName = "Pierre",
                ),
                watchedItems
                    .last()
                    .last()
                    .presentationModel.icon,
            )
        }

    @Test
    fun `GIVEN a native UI watches the actions WHEN clicking a tab THEN receives the navigation`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val watchedActions = mutableListOf<MainScreenUiAction>()
            viewModel.actionWatcher.observe { watchedActions += it }

            // When
            viewModel.onEvent(MainScreenUiEvent.BottomNavItemClicked(MainNavRouteDestination.Books))

            // Then
            assertEquals(
                listOf<MainScreenUiAction>(MainScreenUiAction.NavigateToBottomRoute(MainNavRouteDestination.Books)),
                watchedActions,
            )
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
