package com.quare.bibleplanner.feature.read.presentation.listening.player

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningAllowanceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningTimeUseCase
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.feature.read.fake.FakeChapterListeningController
import com.quare.bibleplanner.feature.read.fixture.listeningSession
import com.quare.bibleplanner.feature.read.presentation.listening.ChapterListeningGate
import com.quare.bibleplanner.feature.read.presentation.listening.ListeningPlaybackActions
import com.quare.bibleplanner.feature.read.presentation.listening.mapper.ListeningPlayerUiModelMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.update
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
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChapterListeningPlayerViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val genesisOne = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 1)
    private val genesisTwo = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 2)
    private val genesisThree = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 3)

    private lateinit var viewModel: ChapterListeningPlayerViewModel
    private lateinit var controller: FakeChapterListeningController
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a chapter being read WHEN opening the player THEN shows its neighbours and settings`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(session = listeningSession(chapter = genesisTwo))

            // When
            val state = viewModel.uiState.value

            // Then
            assertEquals(genesisOne, state.previousChapter)
            assertEquals(genesisThree, state.nextChapter)
            assertEquals(1f, state.speed)
            assertTrue(state.isAutoNextEnabled)
            assertFalse(state.isAutoNextLocked)
            assertEquals(
                ListeningSleepTimerUiModel(
                    selectedOption = ListeningSleepTimerOption.OFF,
                    remaining = null,
                ),
                state.sleepTimer,
            )
        }

    @Test
    fun `GIVEN the reading of today WHEN opening the player THEN keeps auto next on for the day`() =
        runTest(testDispatcher) {
            // Given
            val day = ListeningDayModel(
                location = PlanDayLocationModel(
                    weekNumber = 1,
                    dayNumber = 1,
                    readingPlanType = ReadingPlanType.BOOKS,
                ),
                segments = listOf(genesisOne, genesisThree).map { chapter ->
                    ListeningSegmentModel(
                        chapter = chapter,
                        startVerse = null,
                        endVerse = null,
                    )
                },
            )
            prepareScenario(
                session = listeningSession(
                    chapter = genesisOne,
                    day = day,
                ),
            )

            // When
            val state = viewModel.uiState.value

            // Then
            assertTrue(state.isAutoNextLocked)
            assertTrue(state.isAutoNextEnabled)
        }

    @Test
    fun `GIVEN sleep timers WHEN observing THEN shows the chosen option and the time left`() = runTest(testDispatcher) {
        // Given
        prepareScenario(session = listeningSession(chapter = genesisOne))
        controller.state.update {
            it.copy(
                sleepTimer = ListeningSleepTimerModel.Countdown(
                    option = ListeningSleepTimerOption.FIFTEEN_MINUTES,
                    remaining = 10.minutes,
                ),
            )
        }
        val countdown = viewModel.uiState.value.sleepTimer

        // When
        controller.state.update { it.copy(sleepTimer = ListeningSleepTimerModel.EndOfChapter) }

        // Then
        assertEquals(
            ListeningSleepTimerUiModel(
                selectedOption = ListeningSleepTimerOption.FIFTEEN_MINUTES,
                remaining = 10.minutes,
            ),
            countdown,
        )
        assertEquals(
            ListeningSleepTimerUiModel(
                selectedOption = ListeningSleepTimerOption.END_OF_CHAPTER,
                remaining = null,
            ),
            viewModel.uiState.value.sleepTimer,
        )
    }

    @Test
    fun `GIVEN the player open WHEN listening stops THEN closes it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(session = listeningSession(chapter = genesisOne))

        // When
        controller.setSession(null)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
    }

    @Test
    fun `GIVEN the player controls WHEN using them THEN drives the player`() = runTest(testDispatcher) {
        // Given
        prepareScenario(session = listeningSession(chapter = genesisTwo))

        // When
        listOf(
            ChapterListeningPlayerUiEvent.OnPlayPauseClick,
            ChapterListeningPlayerUiEvent.OnPreviousVerseClick,
            ChapterListeningPlayerUiEvent.OnNextVerseClick,
            ChapterListeningPlayerUiEvent.OnPreviousChapterClick,
            ChapterListeningPlayerUiEvent.OnNextChapterClick,
            ChapterListeningPlayerUiEvent.OnSeek(1),
            ChapterListeningPlayerUiEvent.OnSpeedClick(1.25f),
            ChapterListeningPlayerUiEvent.OnSleepTimerClick(ListeningSleepTimerOption.END_OF_CHAPTER),
            ChapterListeningPlayerUiEvent.OnAutoNextToggle(false),
        ).forEach(viewModel::onEvent)

        // Then
        assertEquals(
            listOf(
                "togglePlayPause",
                "previousVerse",
                "nextVerse",
                "moveToChapter:1:true",
                "moveToChapter:3:true",
                "skipToVerse:1",
                "setSpeed:1.25",
                "setSleepTimer:end_of_chapter",
                "setAutoNextEnabled:false",
            ),
            controller.calls,
        )
        assertTrue(
            trackedEvents.contains(
                "chapter_listening_control_clicked" to mapOf<String, Any>("control" to "pause", "surface" to "player"),
            ),
        )
    }

    @Test
    fun `GIVEN a locked next chapter WHEN skipping to it THEN asks to unlock it and keeps playing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                session = listeningSession(chapter = genesisTwo),
                access = ChapterListeningAccessModel.LimitReached(
                    ListeningAllowanceModel(
                        hasRewardedUnlocks = true,
                        hasFreeUnlocks = true,
                    ),
                ),
            )

            // When
            viewModel.onEvent(ChapterListeningPlayerUiEvent.OnNextChapterClick)

            // Then
            assertTrue(controller.calls.isEmpty())
            assertEquals(
                listOf<NavigationCommand>(
                    NavigationCommand.Navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_LISTENING_LIMIT)),
                ),
                commands,
            )
        }

    @Test
    fun `GIVEN a paused chapter WHEN skipping to a locked one THEN moves without spending an unlock`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                session = listeningSession(
                    chapter = genesisTwo,
                    status = ListeningStatusModel.PAUSED,
                ),
                access = ChapterListeningAccessModel.LimitReached(
                    ListeningAllowanceModel(
                        hasRewardedUnlocks = true,
                        hasFreeUnlocks = true,
                    ),
                ),
            )

            // When
            viewModel.onEvent(ChapterListeningPlayerUiEvent.OnNextChapterClick)

            // Then
            assertEquals(listOf("moveToChapter:3:false"), controller.calls)
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN the last chapter of the order WHEN skipping forward THEN does nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario(session = listeningSession(chapter = genesisThree))

        // When
        viewModel.onEvent(ChapterListeningPlayerUiEvent.OnNextChapterClick)

        // Then
        assertTrue(controller.calls.isEmpty())
    }

    @Test
    fun `GIVEN the next chapter locked WHEN unlocking from the player THEN continues into it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                session = listeningSession(
                    chapter = genesisOne,
                    status = ListeningStatusModel.NEXT_LOCKED,
                    lockedChapter = genesisTwo,
                ),
            )

            // When
            viewModel.onEvent(ChapterListeningPlayerUiEvent.OnUnlockClick)

            // Then
            assertEquals(listOf("continueLockedChapter"), controller.calls)
        }

    @Test
    fun `GIVEN the player open WHEN dismissing it THEN goes back`() = runTest(testDispatcher) {
        // Given
        prepareScenario(session = listeningSession(chapter = genesisOne))

        // When
        viewModel.onEvent(ChapterListeningPlayerUiEvent.OnDismiss)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
    }

    private fun TestScope.prepareScenario(
        session: ListeningSessionModel?,
        access: ChapterListeningAccessModel = ChapterListeningAccessModel.Open,
    ) {
        controller = FakeChapterListeningController(
            session = session,
            readingOrder = listOf(genesisOne, genesisTwo, genesisThree),
        )
        commands = mutableListOf()
        trackedEvents = mutableListOf()
        val navigator = Navigator()
        backgroundScope.launch { navigator.commands.collect { command -> commands += command } }
        val gate = ChapterListeningGate(
            getChapterListeningAccess = { access },
            recordChapterListeningUnlock = { },
            prepareRewardedUnlockOffer = { false },
            checkRewardedUnlockEnabled = { true },
            studyUnlockResultStore = StudyUnlockResultStore(),
            navigator = navigator,
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        val trackEvent = TrackEvent { name, params -> trackedEvents += name to params }
        viewModel = ViewModelProvider.create(
            store = ViewModelStore(),
            factory = viewModelFactory {
                initializer {
                    ChapterListeningPlayerViewModel(
                        controller = controller,
                        gate = gate,
                        playbackActions = ListeningPlaybackActions(
                            controller = controller,
                            gate = gate,
                            navigator = navigator,
                            trackEvent = trackEvent,
                            getAuthenticatedUserId = { null },
                        ),
                        navigator = navigator,
                        playerUiModelMapper = ListeningPlayerUiModelMapper(GetListeningTimeUseCase()),
                        trackEvent = trackEvent,
                    )
                }
            },
        )[ChapterListeningPlayerViewModel::class]
    }
}
