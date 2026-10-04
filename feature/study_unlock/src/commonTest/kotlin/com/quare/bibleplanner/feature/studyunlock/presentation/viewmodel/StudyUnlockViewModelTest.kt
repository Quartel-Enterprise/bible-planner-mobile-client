package com.quare.bibleplanner.feature.studyunlock.presentation.viewmodel

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdAvailability
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdFailureReason
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdResult
import com.quare.bibleplanner.core.provider.ads.testing.FakeRewardedAdService
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockUiEvent
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockUiState
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockVideoState
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class StudyUnlockViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val navigator = Navigator()
    private val route = StudyUnlockNavRoute(
        surface = StudyUnlockSurface.CHAPTER_STUDY,
        paywallSource = PaywallEntrySource.CHAPTER_STUDY,
        requestKey = REQUEST_KEY,
        rewardedRemainingToday = 2,
    )
    private val surfaceParams: Map<String, Any> = mapOf("surface" to "chapter_study")
    private lateinit var viewModel: StudyUnlockViewModel
    private lateinit var rewardedAdService: FakeRewardedAdService
    private lateinit var resultStore: StudyUnlockResultStore
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var earnedNotifications: MutableList<Unit>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN an opened sheet WHEN reading its state THEN it tracked the view and preloaded the video`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(availability = RewardedAdAvailability.LOADING)

            // When
            val uiState = viewModel.uiState.value

            // Then
            assertEquals(
                expected = listOf(
                    "unlock_sheet_viewed" to surfaceParams + mapOf("rewarded_remaining_today" to 2),
                ),
                actual = trackedEvents,
            )
            assertEquals(1, rewardedAdService.preloadCount)
            assertEquals(
                expected = StudyUnlockUiState(
                    surface = StudyUnlockSurface.CHAPTER_STUDY,
                    rewardedRemainingToday = 2,
                    videoState = StudyUnlockVideoState.LOADING,
                ),
                actual = uiState,
            )
        }

    @Test
    fun `GIVEN the video loads WHEN it is ready THEN the watch option is enabled`() = runTest(testDispatcher) {
        // Given
        prepareScenario(availability = RewardedAdAvailability.LOADING)

        // When
        rewardedAdService.availabilityFlow.value = RewardedAdAvailability.READY

        // Then
        assertEquals(StudyUnlockVideoState.READY, viewModel.uiState.value.videoState)
    }

    @Test
    fun `GIVEN no ad to show WHEN the load ends THEN shows the video as unavailable and tracks no fill`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(availability = RewardedAdAvailability.LOADING)

            // When
            rewardedAdService.availabilityFlow.value = RewardedAdAvailability.NO_FILL

            // Then
            assertEquals(StudyUnlockVideoState.UNAVAILABLE, viewModel.uiState.value.videoState)
            assertEquals(
                expected = "rewarded_ad_failed" to surfaceParams + mapOf("reason" to "no_fill"),
                actual = trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN the sheet WHEN subscribing THEN replaces it with the paywall`() = runTest(testDispatcher) {
        // Given
        prepareScenario(availability = RewardedAdAvailability.READY)

        // When
        viewModel.onEvent(StudyUnlockUiEvent.OnSubscribeClick)

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(
                NavigationCommand.NavigateReplacingTop(PaywallNavRoute(PaywallEntrySource.CHAPTER_STUDY)),
            ),
            actual = commands,
        )
        assertEquals("unlock_subscribe_clicked" to surfaceParams, trackedEvents.last())
    }

    @Test
    fun `GIVEN the sheet WHEN dismissing THEN goes back without tracking`() = runTest(testDispatcher) {
        // Given
        prepareScenario(availability = RewardedAdAvailability.READY)

        // When
        viewModel.onEvent(StudyUnlockUiEvent.OnDismiss)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(1, trackedEvents.size)
    }

    @Test
    fun `GIVEN a ready video WHEN the reward is earned THEN closes the sheet and hands the unlock back`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                availability = RewardedAdAvailability.READY,
                showResult = RewardedAdResult.Earned,
            )

            // When
            viewModel.onEvent(StudyUnlockUiEvent.OnWatchVideoClick)

            // Then
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(1, earnedNotifications.size)
            assertEquals(
                expected = listOf("rewarded_ad_started", "rewarded_ad_earned"),
                actual = trackedEvents.drop(1).map { it.first },
            )
        }

    @Test
    fun `GIVEN a ready video WHEN it is closed early THEN stays open and loads another one`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                availability = RewardedAdAvailability.READY,
                showResult = RewardedAdResult.Dismissed,
            )
            rewardedAdService.availabilityFlow.value = RewardedAdAvailability.READY

            // When
            viewModel.onEvent(StudyUnlockUiEvent.OnWatchVideoClick)

            // Then
            assertTrue(commands.isEmpty())
            assertTrue(earnedNotifications.isEmpty())
            assertEquals(2, rewardedAdService.preloadCount)
            assertEquals("rewarded_ad_dismissed" to surfaceParams, trackedEvents.last())
        }

    @Test
    fun `GIVEN a ready video WHEN it fails to show THEN tracks the show error`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            availability = RewardedAdAvailability.READY,
            showResult = RewardedAdResult.Failed(RewardedAdFailureReason.SHOW_ERROR),
        )

        // When
        viewModel.onEvent(StudyUnlockUiEvent.OnWatchVideoClick)

        // Then
        assertTrue(earnedNotifications.isEmpty())
        assertEquals(
            expected = "rewarded_ad_failed" to surfaceParams + mapOf("reason" to "show_error"),
            actual = trackedEvents.last(),
        )
    }

    @Test
    fun `GIVEN a video still loading WHEN tapping watch THEN nothing is shown`() = runTest(testDispatcher) {
        // Given
        prepareScenario(availability = RewardedAdAvailability.LOADING)

        // When
        viewModel.onEvent(StudyUnlockUiEvent.OnWatchVideoClick)

        // Then
        assertEquals(0, rewardedAdService.showCount)
        assertEquals(1, trackedEvents.size)
    }

    private fun TestScope.prepareScenario(
        availability: RewardedAdAvailability,
        showResult: RewardedAdResult = RewardedAdResult.Earned,
    ) {
        rewardedAdService = FakeRewardedAdService(
            isSupported = true,
            availability = availability,
            showResult = showResult,
        )
        resultStore = StudyUnlockResultStore()
        commands = mutableListOf()
        trackedEvents = mutableListOf()
        earnedNotifications = mutableListOf()
        backgroundScope.launch { navigator.commands.collect { commands += it } }
        backgroundScope.launch { resultStore.observeEarned(REQUEST_KEY).collect { earnedNotifications += it } }
        viewModel = StudyUnlockViewModel(
            route = route,
            navigator = navigator,
            rewardedAdService = rewardedAdService,
            resultStore = resultStore,
            trackEvent = { name, params -> trackedEvents += name to params },
        )
    }

    private companion object {
        const val REQUEST_KEY = "chapter_study|GEN|3"
    }
}
