package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_limit_reached_message
import bibleplanner.feature.day_study.generated.resources.ai_study_wait_for_generations
import com.quare.bibleplanner.core.books.util.getReadingLabel
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.model.route.ChatEntrySource
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.FakeDayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.model.ChapterSummaryModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.feature.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayPassagesForDayStudyUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.feature.daystudy.fake.DefaultIntRemoteConfig
import com.quare.bibleplanner.feature.daystudy.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.daystudy.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.daystudy.fake.FakeDayStudyRepository
import com.quare.bibleplanner.feature.daystudy.fake.FakePlanRepository
import com.quare.bibleplanner.feature.daystudy.presentation.factory.DayStudyCardUiModelFactory
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationError
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyRouteUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class DayStudyRouteViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testTimeout = 5.seconds
    private val route = DayStudyNavRoute(
        dayNumber = 2,
        weekNumber = 3,
        readingPlanType = "BOOKS",
    )
    private val dayRoute = DayNavRoute(
        dayNumber = 2,
        weekNumber = 3,
        readingPlanType = "BOOKS",
    )
    private val jobKey = "BOOKS|3|2"
    private val passages = listOf(
        PassageModel(
            bookId = BookId.GEN,
            chapters = listOf(
                ChapterModel(
                    number = 1,
                    startVerse = null,
                    endVerse = null,
                    bookId = BookId.GEN,
                ),
            ),
            isRead = false,
            chapterRanges = "1",
        ),
    )
    private val study = DayStudyModel(
        passageLabel = "Genesis 1",
        overview = "In the beginning",
        chapterSummaries = listOf(
            ChapterSummaryModel(
                title = "Creation",
                body = "Six days",
            ),
        ),
        takeaways = listOf("God creates"),
        context = HistoricalContextModel(
            body = "Ancient Near East",
            facts = emptyList(),
        ),
        commonQuestions = emptyList(),
    )
    private lateinit var viewModel: DayStudyRouteViewModel
    private lateinit var viewModelStore: ViewModelStore
    private lateinit var repository: FakeDayStudyRepository
    private lateinit var coordinator: FakeDayStudyGenerationCoordinator
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var actions: MutableList<DayStudyRouteUiAction>
    private lateinit var commands: MutableList<NavigationCommand>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a day with passages WHEN opening the study THEN marks the job active and shows the reading label`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario()

            // When
            val state = awaitPassages()

            // Then
            assertEquals(passages.getReadingLabel(), state.passageLabel)
            assertEquals(listOf(jobKey), coordinator.activatedKeys)
            assertEquals(Platform.Android, state.platform)
        }

    @Test
    fun `GIVEN free quota left WHEN the card loads THEN offers to generate and tracks the load`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(usedCount = 1)

            // When
            awaitPassages()

            // Then
            assertEquals(
                Loadable.Loaded(
                    DayStudyCardQuotaUiModel(
                        remainingFree = 2,
                        freeLimit = 3,
                    ),
                ),
                card().quota,
            )
            assertEquals(DayStudyCardMode.GENERATE, card().mode)
            val load = trackedParams(AnalyticsEventNames.DAY_STUDY_LOAD)
            assertEquals("panel", load?.get(AnalyticsParams.TARGET))
            assertEquals(true, load?.get(AnalyticsParams.SUCCESS))
            assertEquals(false, load?.get(AnalyticsParams.IS_CACHED))
        }

    @Test
    fun `GIVEN a cached study WHEN the card loads THEN shows it as viewable and tracks a single cached load`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(hasCached = true)

            // When
            awaitPassages()

            // Then
            assertEquals(DayStudyCardMode.VIEW, card().mode)
            val loads = trackedEvents.filter { (name, _) -> name == AnalyticsEventNames.DAY_STUDY_LOAD }
            assertEquals(1, loads.size)
            assertEquals(true, loads.single().second[AnalyticsParams.IS_CACHED])
        }

    @Test
    fun `GIVEN the quota request fails WHEN the card loads THEN keeps loading and tracks the failure reason`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(statusError = IllegalStateException("boom"))

            // When
            awaitPassages()

            // Then
            assertEquals(Loadable.Loading, viewModel.uiState.value.card)
            val load = trackedParams(AnalyticsEventNames.DAY_STUDY_LOAD)
            assertEquals(false, load?.get(AnalyticsParams.SUCCESS))
            assertEquals("IllegalStateException", load?.get(AnalyticsParams.REASON))
        }

    @Test
    fun `GIVEN an exhausted free quota WHEN tapping the card THEN opens the paywall`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(usedCount = 3)
            awaitPassages()

            // When
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()

            // Then
            assertEquals(
                listOf<NavigationCommand>(
                    NavigationCommand.Navigate(PaywallNavRoute(PaywallEntrySource.DAY_STUDY_DETAIL)),
                ),
                commands,
            )
            assertEquals(
                mapOf<String, Any>(
                    AnalyticsParams.CARD_MODE to "locked",
                    AnalyticsParams.IS_PRO to false,
                    AnalyticsParams.SOURCE to "day_study_detail",
                ),
                trackedParams(AnalyticsEventNames.DAY_STUDY_CARD_CLICKED),
            )
        }

    @Test
    fun `GIVEN a signed out reader WHEN tapping generate THEN asks to sign in`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(userId = null)
            awaitPassages()

            // When
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()

            // Then
            assertEquals(
                listOf<NavigationCommand>(
                    NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.DayStudy.key)),
                ),
                commands,
            )
            assertTrue(coordinator.startedJobs.isEmpty())
            assertFalse(viewModel.uiState.value.isOpeningStudy)
        }

    @Test
    fun `GIVEN free quota left WHEN tapping generate THEN starts the generation and tracks it`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(usedCount = 1)
            val state = awaitPassages()

            // When
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()

            // Then
            assertEquals(listOf(Triple(passages, dayRoute, state.passageLabel.orEmpty())), coordinator.startedJobs)
            assertEquals(DayStudyGenerationUiModel(currentPhaseIndex = 0), viewModel.uiState.value.generation)
            assertEquals(
                mapOf<String, Any>(
                    AnalyticsParams.PLAN_TYPE to "books",
                    AnalyticsParams.WEEK_NUMBER to 3,
                    AnalyticsParams.DAY_NUMBER to 2,
                    AnalyticsParams.IS_PRO to false,
                    AnalyticsParams.REMAINING_FREE to 2,
                ),
                trackedParams(AnalyticsEventNames.DAY_STUDY_GENERATION_STARTED),
            )
        }

    @Test
    fun `GIVEN a pro reader WHEN tapping generate THEN starts even with other generations running`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(
                usedCount = 3,
                isPro = true,
                generatingCount = 5,
            )
            awaitPassages()

            // When
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()

            // Then
            assertEquals(1, coordinator.startedJobs.size)
        }

    @Test
    fun `GIVEN no connection WHEN tapping generate THEN shows the offline error and tracks the failure`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(isConnected = MutableStateFlow(false))
            awaitPassages()

            // When
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()

            // Then
            assertEquals(DayStudyGenerationError.OFFLINE, viewModel.uiState.value.generationError)
            assertEquals(
                "offline",
                trackedParams(AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED)?.get(AnalyticsParams.REASON),
            )
            assertTrue(coordinator.startedJobs.isEmpty())
        }

    @Test
    fun `GIVEN other generations using the free quota WHEN tapping generate THEN asks to wait for them`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(
                usedCount = 1,
                generatingCount = 2,
            )
            awaitPassages()

            // When
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()

            // Then
            assertEquals(
                listOf<DayStudyRouteUiAction>(
                    DayStudyRouteUiAction.ShowSnackBarPlural(
                        resource = Res.plurals.ai_study_wait_for_generations,
                        count = 2,
                    ),
                ),
                actions,
            )
            assertTrue(coordinator.startedJobs.isEmpty())
        }

    @Test
    fun `GIVEN a cached study WHEN tapping view THEN opens it and tracks a cached open`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(
                hasCached = true,
                events = listOf(
                    DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING),
                    DayStudyGenerationEventModel.Completed(study),
                ),
            )
            awaitPassages()

            // When
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()

            // Then
            assertEquals(study, viewModel.uiState.value.openStudy)
            assertEquals(
                mapOf<String, Any>(AnalyticsParams.IS_CACHED to true),
                trackedParams(AnalyticsEventNames.DAY_STUDY_OPENED),
            )
            assertTrue(coordinator.startedJobs.isEmpty())
        }

    @Test
    fun `GIVEN an unlocked day without a local copy WHEN tapping view THEN generates it again`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(
                usedCount = 3,
                isUnlocked = true,
            )
            awaitPassages()

            // When
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()

            // Then
            assertEquals(DayStudyCardMode.VIEW, card().mode)
            assertEquals(1, coordinator.startedJobs.size)
        }

    @Test
    fun `GIVEN an open study WHEN tapping the card THEN only tracks the click`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(
                hasCached = true,
                events = listOf(DayStudyGenerationEventModel.Completed(study)),
            )
            awaitPassages()
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()

            // When
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()

            // Then
            assertEquals(2, trackedEvents.count { (name, _) -> name == AnalyticsEventNames.DAY_STUDY_CARD_CLICKED })
            assertEquals(1, trackedEvents.count { (name, _) -> name == AnalyticsEventNames.DAY_STUDY_OPENED })
        }

    @Test
    fun `GIVEN the card still loading WHEN tapping it THEN ignores the tap`() = runTest(testDispatcher, testTimeout) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
        runCurrent()

        // Then
        assertTrue(trackedEvents.none { (name, _) -> name == AnalyticsEventNames.DAY_STUDY_CARD_CLICKED })
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN an offline error WHEN retrying online THEN clears the error and starts the generation`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            val isConnected = MutableStateFlow(false)
            prepareScenario(isConnected = isConnected)
            awaitPassages()
            viewModel.onEvent(DayStudyRouteUiEvent.OnCardClick)
            runCurrent()
            isConnected.value = true

            // When
            viewModel.onEvent(DayStudyRouteUiEvent.OnRetryClick)
            runCurrent()

            // Then
            assertNull(viewModel.uiState.value.generationError)
            assertEquals(1, coordinator.startedJobs.size)
            assertEquals(emptyMap(), trackedParams(AnalyticsEventNames.DAY_STUDY_RETRY_CLICKED))
        }

    @Test
    fun `GIVEN the study WHEN asking the AI THEN opens the chat for this day`() = runTest(testDispatcher, testTimeout) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DayStudyRouteUiEvent.OnAskAiClick)
        runCurrent()

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.Navigate(
                    ChatNavRoute(
                        source = ChatEntrySource.DAY_STUDY_QUESTIONS,
                        dayNumber = 2,
                        weekNumber = 3,
                        readingPlanType = "BOOKS",
                    ),
                ),
            ),
            commands,
        )
        assertEquals(
            mapOf<String, Any>(AnalyticsParams.SOURCE to "day_study_questions"),
            trackedParams(AnalyticsEventNames.AI_CHAT_ENTRY_CLICKED),
        )
    }

    @Test
    fun `GIVEN a running job WHEN its phase advances THEN shows the matching generation step`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario()
            awaitPassages()

            // When
            coordinator.jobsFlow.value =
                listOf(
                    job(
                        status = DayStudyGenerationStatus.Generating,
                        phase = DayStudyPhaseModel.CONTEXT,
                    ),
                )
            runCurrent()

            // Then
            assertEquals(DayStudyGenerationUiModel(currentPhaseIndex = 2), viewModel.uiState.value.generation)
            assertNull(viewModel.uiState.value.generationError)
        }

    @Test
    fun `GIVEN a running job WHEN it finishes THEN completes the steps and opens the study`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario()
            awaitPassages()
            coordinator.jobsFlow.value = listOf(
                job(
                    status = DayStudyGenerationStatus.Generating,
                    phase = null,
                ),
            )
            runCurrent()

            // When
            coordinator.jobsFlow.value = listOf(
                job(
                    status = DayStudyGenerationStatus.Done(study),
                    phase = null,
                ),
            )
            advanceUntilIdle()

            // Then
            assertEquals(study, viewModel.uiState.value.openStudy)
            assertNull(viewModel.uiState.value.generation)
            assertEquals(listOf(jobKey), coordinator.acknowledgedKeys)
            assertEquals(
                mapOf<String, Any>(AnalyticsParams.IS_CACHED to false),
                trackedParams(AnalyticsEventNames.DAY_STUDY_OPENED),
            )
        }

    @Test
    fun `GIVEN a job WHEN it hits the free limit THEN locks the card and explains why`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario(usedCount = 1)
            awaitPassages()

            // When
            coordinator.jobsFlow.value = listOf(
                job(
                    status = DayStudyGenerationStatus.Failed(
                        isLimitReached = true,
                        isOffline = false,
                    ),
                    phase = null,
                ),
            )
            runCurrent()

            // Then
            assertEquals(DayStudyCardMode.LOCKED, card().mode)
            assertEquals(
                Loadable.Loaded(
                    DayStudyCardQuotaUiModel(
                        remainingFree = 0,
                        freeLimit = 3,
                    ),
                ),
                card().quota,
            )
            assertNull(viewModel.uiState.value.generationError)
            assertEquals(
                listOf<DayStudyRouteUiAction>(
                    DayStudyRouteUiAction.ShowSnackBar(Res.string.ai_study_limit_reached_message),
                ),
                actions,
            )
            assertEquals(listOf(jobKey), coordinator.acknowledgedKeys)
        }

    @Test
    fun `GIVEN a job WHEN it fails offline or for another reason THEN shows the matching error`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario()
            awaitPassages()

            // When
            coordinator.jobsFlow.value = listOf(
                job(
                    status = DayStudyGenerationStatus.Failed(
                        isLimitReached = false,
                        isOffline = true,
                    ),
                    phase = null,
                ),
            )
            runCurrent()
            val offlineError = viewModel.uiState.value.generationError
            coordinator.jobsFlow.value = listOf(
                job(
                    status = DayStudyGenerationStatus.Failed(
                        isLimitReached = false,
                        isOffline = false,
                    ),
                    phase = null,
                ),
            )
            runCurrent()

            // Then
            assertEquals(DayStudyGenerationError.OFFLINE, offlineError)
            assertEquals(DayStudyGenerationError.GENERIC, viewModel.uiState.value.generationError)
            assertTrue(actions.isEmpty())
        }

    @Test
    fun `GIVEN a job for another day WHEN it updates THEN ignores it`() = runTest(testDispatcher, testTimeout) {
        // Given
        prepareScenario()
        awaitPassages()

        // When
        coordinator.jobsFlow.value = listOf(
            job(
                status = DayStudyGenerationStatus.Generating,
                phase = DayStudyPhaseModel.QUESTIONS,
            ).copy(key = "other"),
        )
        runCurrent()

        // Then
        assertNull(viewModel.uiState.value.generation)
    }

    @Test
    fun `GIVEN an open study screen WHEN it is closed THEN releases the active job`() =
        runTest(testDispatcher, testTimeout) {
            // Given
            prepareScenario()

            // When
            viewModelStore.clear()

            // Then
            assertEquals(listOf(jobKey), coordinator.clearedKeys)
        }

    private suspend fun TestScope.awaitPassages(): DayStudyRouteUiState {
        val state = viewModel.uiState.first { it.passageLabel != null }
        runCurrent()
        return state
    }

    private fun card(): DayStudyCardUiModel = requireNotNull(
        viewModel.uiState.value.card
            .valueOrNull(),
    )

    private fun trackedParams(name: String): Map<String, Any>? =
        trackedEvents.lastOrNull { (eventName, _) -> eventName == name }?.second

    private fun job(
        status: DayStudyGenerationStatus,
        phase: DayStudyPhaseModel?,
    ): DayStudyGenerationJob = DayStudyGenerationJob(
        key = jobKey,
        label = "Genesis 1",
        dayRoute = dayRoute,
        phase = phase,
        status = status,
    )

    private fun TestScope.prepareScenario(
        hasCached: Boolean = false,
        usedCount: Int = 0,
        isUnlocked: Boolean = false,
        statusError: Throwable? = null,
        events: List<DayStudyGenerationEventModel> = emptyList(),
        isPro: Boolean = false,
        userId: String? = "user-id",
        isConnected: MutableStateFlow<Boolean> = MutableStateFlow(true),
        generatingCount: Int = 0,
    ) {
        trackedEvents = mutableListOf()
        actions = mutableListOf()
        commands = mutableListOf()
        repository = FakeDayStudyRepository(
            hasCached = hasCached,
            status = DayStudyStatusModel(
                freeLimit = 3,
                usedCount = usedCount,
                isUnlocked = isUnlocked,
                cacheToken = "token",
            ),
            statusError = statusError,
            events = events,
        )
        coordinator = FakeDayStudyGenerationCoordinator().apply {
            this.generatingCount = generatingCount
        }
        val navigator = Navigator()
        val bibleRepository = FakeBibleRepository()
        val getAppLanguageFlow = { flowOf(Language.ENGLISH) }
        val languageCodeMapper = LanguageCodeMapper()
        viewModelStore = ViewModelStore()
        viewModel = ViewModelProvider.create(
            store = viewModelStore,
            factory = viewModelFactory {
                initializer {
                    DayStudyRouteViewModel(
                        getDayPassages = GetDayPassagesForDayStudyUseCase(
                            GetPlansByWeekUseCase(
                                planRepository = FakePlanRepository(
                                    listOf(
                                        WeekPlanModel(
                                            number = 3,
                                            days = listOf(
                                                DayModel(
                                                    number = 2,
                                                    passages = passages,
                                                    isRead = false,
                                                    totalVerses = 0,
                                                    readVerses = 0,
                                                    readTimestamp = null,
                                                    plannedReadDate = null,
                                                    notes = null,
                                                    isToday = false,
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                                booksRepository = FakeBooksRepository(),
                                getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                                currentTimestampProvider = { 0L },
                                localDateTimeProvider = { LocalDateTime(LocalDate(2026, 1, 1), LocalTime(8, 0)) },
                            ),
                        ),
                        getDayStudy = GetDayStudyUseCase(
                            repository = repository,
                            bibleRepository = bibleRepository,
                            getAppLanguageFlow = getAppLanguageFlow,
                            languageCodeMapper = languageCodeMapper,
                        ),
                        getDayStudyQuota = GetDayStudyQuotaUseCase(
                            repository = repository,
                            bibleRepository = bibleRepository,
                            getAppLanguageFlow = getAppLanguageFlow,
                            languageCodeMapper = languageCodeMapper,
                            getIntRemoteConfig = DefaultIntRemoteConfig(),
                        ),
                        hasCachedStudy = HasCachedStudyUseCase(
                            repository = repository,
                            bibleRepository = bibleRepository,
                            getAppLanguageFlow = getAppLanguageFlow,
                            languageCodeMapper = languageCodeMapper,
                        ),
                        isConnected = { isConnected.value },
                        generationCoordinator = coordinator,
                        observeIsProUser = { flowOf(isPro) },
                        observeAuthenticatedUserId = { flowOf(userId) },
                        cardUiModelFactory = DayStudyCardUiModelFactory(),
                        navigator = navigator,
                        route = route,
                        platform = Platform.Android,
                        trackEvent = { name, params -> trackedEvents += name to params },
                    )
                }
            },
        )[DayStudyRouteViewModel::class]
        backgroundScope.launch { viewModel.uiAction.collect { actions += it } }
        backgroundScope.launch { navigator.commands.collect { commands += it } }
    }
}
