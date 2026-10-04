package com.quare.bibleplanner.feature.read.presentation.listening

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.quare.bibleplanner.core.books.testing.FakeBooksRepository
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.EstimateListeningTimeUseCase
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.ChapterListeningPlayerNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.fake.FakeChapterListeningController
import com.quare.bibleplanner.feature.read.fixture.listeningSession
import com.quare.bibleplanner.feature.read.presentation.listening.mapper.ListeningPlayerUiModelMapper
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningEntrySource
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningFinishOfferUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiAction
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.ui.theme.font.ReaderFont
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReadListeningViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val genesisOne = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 1)
    private val genesisTwo = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 2)
    private val route = ReadNavRoute(
        bookId = BookId.GEN.name,
        chapterNumber = 1,
        isChapterRead = false,
        isFromBookDetails = true,
        targetVerseNumbers = emptyList(),
    )
    private val today = ListeningDayModel(
        location = PlanDayLocationModel(
            weekNumber = 1,
            dayNumber = 1,
            readingPlanType = ReadingPlanType.BOOKS,
        ),
        segments = listOf(genesisOne, genesisTwo).map { chapter ->
            ListeningSegmentModel(
                chapter = chapter,
                startVerse = null,
                endVerse = null,
            )
        },
    )

    private lateinit var viewModel: ReadListeningViewModel
    private lateinit var viewModelStore: ViewModelStore
    private lateinit var controller: FakeChapterListeningController
    private lateinit var followRequests: ListeningFollowRequests
    private lateinit var booksRepository: FakeBooksRepository
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var actions: MutableList<ReadListeningUiAction>
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
    fun `GIVEN a reader the person opened WHEN it starts THEN attaches and pulls the player to its chapter`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            val state = viewModel.uiState.value

            // Then
            assertEquals(1, controller.attachedReaders)
            assertEquals(listOf(genesisOne), controller.followedChapters)
            assertTrue(state.isAvailable)
            assertEquals(listOf(genesisOne, genesisTwo), state.todayChapters)
        }

    @Test
    fun `GIVEN a reader opened because the player moved on WHEN it starts THEN leaves the player alone`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isFollowRequested = true)

            // When
            val followed = controller.followedChapters

            // Then
            assertTrue(followed.isEmpty())
        }

    @Test
    fun `GIVEN the reader is cleared WHEN it leaves THEN detaches from the player`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModelStore.clear()

        // Then
        assertEquals(0, controller.attachedReaders)
    }

    @Test
    fun `GIVEN nothing playing WHEN listening to a chapter outside today THEN starts the chapter in canon order`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(today = null)

            // When
            viewModel.onEvent(
                ReadListeningUiEvent.OnListenClick(
                    chapter = genesisOne,
                    source = ListeningEntrySource.BOTTOM_BAR,
                ),
            )

            // Then
            assertEquals(listOf(genesisOne to true), controller.startedChapters)
            assertEquals(
                "chapter_listening_entry_clicked" to mapOf<String, Any>("source" to "bottom_bar", "is_active" to false),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN a chapter of the reading of today WHEN listening to it THEN plays the day from it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(
                ReadListeningUiEvent.OnListenClick(
                    chapter = genesisTwo,
                    source = ListeningEntrySource.SHORTCUT,
                ),
            )

            // Then
            assertEquals(listOf(today to genesisTwo), controller.startedDays)
        }

    @Test
    fun `GIVEN the chapter already playing WHEN tapping listen THEN opens the player`() = runTest(testDispatcher) {
        // Given
        prepareScenario(session = listeningSession(chapter = genesisOne))

        // When
        viewModel.onEvent(
            ReadListeningUiEvent.OnListenClick(
                chapter = genesisOne,
                source = ListeningEntrySource.HEADER,
            ),
        )

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.Navigate(ChapterListeningPlayerNavRoute)), commands)
        assertTrue(controller.startedChapters.isEmpty())
    }

    @Test
    fun `GIVEN a locked chapter WHEN tapping listen THEN does not play it yet`() = runTest(testDispatcher) {
        // Given
        prepareScenario(access = ChapterListeningAccessModel.LimitReached)

        // When
        viewModel.onEvent(
            ReadListeningUiEvent.OnListenClick(
                chapter = genesisOne,
                source = ListeningEntrySource.BOTTOM_BAR,
            ),
        )

        // Then
        assertTrue(controller.startedChapters.isEmpty())
        assertTrue(controller.startedDays.isEmpty())
    }

    @Test
    fun `GIVEN a playing chapter WHEN using the mini player THEN drives the player`() = runTest(testDispatcher) {
        // Given
        prepareScenario(session = listeningSession(chapter = genesisOne))

        // When
        viewModel.onEvent(ReadListeningUiEvent.OnPlayPauseClick)
        viewModel.onEvent(ReadListeningUiEvent.OnNextVerseClick)
        viewModel.onEvent(ReadListeningUiEvent.OnVoiceSettingsClick)
        viewModel.onEvent(ReadListeningUiEvent.OnFinishOfferDismissClick)
        viewModel.onEvent(ReadListeningUiEvent.OnBackToVerseClick)
        viewModel.onEvent(ReadListeningUiEvent.OnMiniPlayerClick)
        viewModel.onEvent(ReadListeningUiEvent.OnCloseClick)

        // Then
        assertEquals(
            listOf("togglePlayPause", "nextVerse", "openVoiceSettings", "dismissFinishOffer", "stop"),
            controller.calls,
        )
        assertEquals(listOf<NavigationCommand>(NavigationCommand.Navigate(ChapterListeningPlayerNavRoute)), commands)
        assertTrue(
            trackedEvents.contains(
                "chapter_listening_control_clicked" to
                    mapOf<String, Any>("control" to "pause", "surface" to "mini_player"),
            ),
        )
    }

    @Test
    fun `GIVEN an interrupted or paused chapter WHEN tapping play THEN reports resume or play`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                session = listeningSession(
                    chapter = genesisOne,
                    status = ListeningStatusModel.INTERRUPTED,
                ),
            )
            viewModel.onEvent(ReadListeningUiEvent.OnPlayPauseClick)
            controller.setSession(
                listeningSession(
                    chapter = genesisOne,
                    status = ListeningStatusModel.PAUSED,
                ),
            )

            // When
            viewModel.onEvent(ReadListeningUiEvent.OnPlayPauseClick)

            // Then
            val controls = trackedEvents
                .filter { (name, _) -> name == "chapter_listening_control_clicked" }
                .map { (_, params) -> params.getValue("control") }
            assertEquals(listOf<Any>("resume", "play"), controls)
        }

    @Test
    fun `GIVEN nothing playing WHEN tapping play THEN does nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ReadListeningUiEvent.OnPlayPauseClick)
        viewModel.onEvent(ReadListeningUiEvent.OnUnlockNextClick)

        // Then
        assertTrue(controller.calls.isEmpty())
    }

    @Test
    fun `GIVEN the next chapter locked WHEN unlocking it THEN continues into it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            session = listeningSession(
                chapter = genesisOne,
                status = ListeningStatusModel.NEXT_LOCKED,
                lockedChapter = genesisTwo,
            ),
        )

        // When
        viewModel.onEvent(ReadListeningUiEvent.OnUnlockNextClick)

        // Then
        assertEquals(listOf("continueLockedChapter"), controller.calls)
    }

    @Test
    fun `GIVEN the session on this chapter WHEN the player moves on by itself THEN opens the next chapter in place`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(session = listeningSession(chapter = genesisOne))

            // When
            controller.setSession(
                listeningSession(
                    chapter = genesisTwo,
                    cause = ChapterChangeCause.PLAYER,
                ),
            )

            // Then
            assertEquals(
                listOf<NavigationCommand>(
                    NavigationCommand.NavigateReplacing(
                        current = route,
                        route = route.copy(
                            chapterNumber = 2,
                            isChapterRead = true,
                        ),
                    ),
                ),
                commands,
            )
            assertTrue(followRequests.consume(genesisTwo))
        }

    @Test
    fun `GIVEN the session on this chapter WHEN another reader takes it THEN stays on this chapter`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(session = listeningSession(chapter = genesisOne))

            // When
            controller.setSession(
                listeningSession(
                    chapter = genesisTwo,
                    cause = ChapterChangeCause.READER,
                ),
            )

            // Then
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN vertical reading WHEN the player moves on THEN keeps the same list`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            session = listeningSession(chapter = genesisOne),
            isVerticalReadingEnabled = true,
        )

        // When
        controller.setSession(
            listeningSession(
                chapter = genesisTwo,
                cause = ChapterChangeCause.PLAYER,
            ),
        )

        // Then
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN an unread chapter heard to the end WHEN observing THEN offers to mark it read`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            controller.setSession(
                listeningSession(
                    chapter = genesisTwo,
                    cause = ChapterChangeCause.READER,
                    finishedChapter = genesisOne,
                ),
            )

            // Then
            assertEquals(
                ListeningFinishOfferUiModel(
                    chapter = genesisOne,
                    playingChapter = genesisTwo,
                ),
                viewModel.uiState.value.finishOffer,
            )
        }

    @Test
    fun `GIVEN a chapter heard to the end that is already read WHEN observing THEN offers nothing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            controller.setSession(
                listeningSession(
                    chapter = genesisTwo,
                    status = ListeningStatusModel.FINISHED,
                    finishedChapter = genesisTwo,
                ),
            )

            // Then
            assertNull(viewModel.uiState.value.finishOffer)
        }

    @Test
    fun `GIVEN the sleep timer ended WHEN the player says so THEN shows the good night message`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            controller.events.emit(ChapterListeningEventModel.SleepTimerEnded)

            // Then
            assertEquals(listOf<ReadListeningUiAction>(ReadListeningUiAction.ShowSleepTimerEnded), actions)
        }

    @Test
    fun `GIVEN listening turned off WHEN observing THEN hides it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isEnabled = false)

        // When
        val state = viewModel.uiState.value

        // Then
        assertFalse(state.isAvailable)
    }

    private fun TestScope.prepareScenario(
        session: ListeningSessionModel? = null,
        isFollowRequested: Boolean = false,
        today: ListeningDayModel? = this@ReadListeningViewModelTest.today,
        access: ChapterListeningAccessModel = ChapterListeningAccessModel.Open,
        isVerticalReadingEnabled: Boolean = false,
        isEnabled: Boolean = true,
    ) {
        controller = FakeChapterListeningController(session = session)
        followRequests = ListeningFollowRequests()
        if (isFollowRequested) followRequests.request(genesisOne)
        booksRepository = FakeBooksRepository(
            listOf(
                BookDataModel(
                    id = BookId.GEN,
                    chapters = listOf(1 to false, 2 to true).map { (number, isRead) ->
                        BookChapterModel(
                            number = number,
                            verses = emptyList(),
                            isRead = isRead,
                            readUpdatedAt = null,
                        )
                    },
                    isRead = false,
                    isFavorite = false,
                ),
            ),
        )
        commands = mutableListOf()
        actions = mutableListOf()
        trackedEvents = mutableListOf()
        val navigator = Navigator()
        backgroundScope.launch { navigator.commands.collect { command -> commands += command } }
        val settings = ReaderSettingsModel(
            fontSizeSp = ReaderFontSize.DEFAULT,
            font = ReaderFont.LORA,
            isRulerEnabled = false,
            rulerLines = ReaderRulerLines.DEFAULT,
            isFocusedVerseEnabled = false,
            isVerticalReadingEnabled = isVerticalReadingEnabled,
            isNoteIconEnabled = true,
        )
        val gate = ChapterListeningGate(
            getChapterListeningAccess = { access },
            recordChapterListeningUnlock = { },
            prepareRewardedUnlockOffer = { false },
            studyUnlockResultStore = StudyUnlockResultStore(),
            navigator = navigator,
        )
        val trackEvent = TrackEvent { name, params -> trackedEvents += name to params }
        viewModelStore = ViewModelStore()
        viewModel = ViewModelProvider.create(
            store = viewModelStore,
            factory = viewModelFactory {
                initializer {
                    ReadListeningViewModel(
                        route = route,
                        controller = controller,
                        gate = gate,
                        playbackActions = ListeningPlaybackActions(
                            controller = controller,
                            gate = gate,
                            trackEvent = trackEvent,
                        ),
                        followRequests = followRequests,
                        navigator = navigator,
                        isWholeChapterRead = { chapterNumber, _ -> chapterNumber == 2 },
                        booksRepository = booksRepository,
                        playerUiModelMapper = ListeningPlayerUiModelMapper(EstimateListeningTimeUseCase()),
                        observeIsChapterListeningEnabled = { flowOf(isEnabled) },
                        observeReaderSettings = { flowOf(settings) },
                        getTodayListeningDay = { today },
                        trackEvent = trackEvent,
                    )
                }
            },
        )[ReadListeningViewModel::class]
        backgroundScope.launch { viewModel.uiAction.collect { action -> actions += action } }
        commands.clear()
    }
}
