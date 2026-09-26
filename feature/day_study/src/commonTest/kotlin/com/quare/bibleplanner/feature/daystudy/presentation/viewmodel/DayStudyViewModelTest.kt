package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import bibleplanner.feature.day_study.generated.resources.Res
import bibleplanner.feature.day_study.generated.resources.ai_study_offline_message
import bibleplanner.feature.day_study.generated.resources.ai_study_wait_for_generations
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.FakeDayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.feature.daystudy.fake.DefaultIntRemoteConfig
import com.quare.bibleplanner.feature.daystudy.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.daystudy.fake.FakeDayStudyRepository
import com.quare.bibleplanner.feature.daystudy.fake.dayStudyModel
import com.quare.bibleplanner.feature.daystudy.presentation.factory.DayStudyCardUiModelFactory
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyGenerationUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiAction
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class DayStudyViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
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
            chapterRanges = null,
        ),
    )
    private val dayRoute = DayNavRoute(
        dayNumber = 1,
        weekNumber = 1,
        readingPlanType = "ONE_YEAR",
    )
    private val otherDayRoute = DayNavRoute(
        dayNumber = 2,
        weekNumber = 1,
        readingPlanType = "ONE_YEAR",
    )
    private val jobKey = "ONE_YEAR|1|1"
    private lateinit var viewModel: DayStudyViewModel
    private lateinit var viewModelStore: ViewModelStore
    private lateinit var coordinator: FakeDayStudyGenerationCoordinator
    private lateinit var repository: FakeDayStudyRepository
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var actions: MutableList<DayStudyUiAction>

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
    fun `GIVEN a cached study and a pro flow that never emits WHEN starting THEN card loads as view immediately`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                hasCached = true,
                status = null,
                observeIsProUser = { emptyFlow() },
            )

            // When
            start()

            // Then
            val card = viewModel.uiState.value.card
                .valueOrNull()
            assertEquals(DayStudyCardMode.VIEW, card?.mode)
            assertIs<Loadable.Loading>(card?.quota)
        }

    @Test
    fun `GIVEN a cached study WHEN the quota refresh lands THEN quota upgrades keeping view mode`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                hasCached = true,
                status = status(
                    usedCount = 1,
                    isUnlocked = true,
                ),
            )

            // When
            start()
            advanceUntilIdle()

            // Then
            val card = viewModel.uiState.value.card
                .valueOrNull()
            assertEquals(DayStudyCardMode.VIEW, card?.mode)
            assertEquals(
                Loadable.Loaded(
                    DayStudyCardQuotaUiModel(
                        remainingFree = 2,
                        freeLimit = 3,
                    ),
                ),
                card?.quota,
            )
        }

    @Test
    fun `GIVEN a cached study WHEN starting THEN tracks day_study_load once as cached`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = true,
            status = null,
        )

        // When
        start()
        advanceUntilIdle()

        // Then
        val loads = trackedEvents.filter { it.first == AnalyticsEventNames.DAY_STUDY_LOAD }
        assertEquals(1, loads.size)
        val params = loads.single().second
        assertEquals("card", params[AnalyticsParams.TARGET])
        assertEquals(true, params[AnalyticsParams.IS_CACHED])
        assertEquals(true, params[AnalyticsParams.SUCCESS])
    }

    @Test
    fun `GIVEN no cached study WHEN the quota resolves THEN card loads as generate`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = false,
            status = null,
        )

        // When
        start()
        advanceUntilIdle()

        // Then
        val card = viewModel.uiState.value.card
            .valueOrNull()
        assertEquals(DayStudyCardMode.GENERATE, card?.mode)
        assertEquals(
            Loadable.Loaded(
                DayStudyCardQuotaUiModel(
                    remainingFree = 3,
                    freeLimit = 3,
                ),
            ),
            card?.quota,
        )
    }

    @Test
    fun `GIVEN the quota request fails WHEN starting THEN tracks a failed load and keeps the card loading`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                hasCached = false,
                status = null,
                statusError = IllegalStateException("offline"),
            )

            // When
            start()

            // Then
            assertEquals(Loadable.Loading, viewModel.uiState.value.card)
            val load = trackedEvents.single { (name, _) -> name == AnalyticsEventNames.DAY_STUDY_LOAD }.second
            assertEquals(false, load[AnalyticsParams.SUCCESS])
            assertEquals("IllegalStateException", load[AnalyticsParams.REASON])
        }

    @Test
    fun `GIVEN the card still loading WHEN tapping it THEN ignores the tap`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = false,
            status = null,
        )

        // When
        viewModel.onEvent(DayStudyUiEvent.OnCardClick)

        // Then
        assertTrue(trackedEvents.isEmpty())
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `GIVEN free quota left WHEN tapping generate THEN starts the generation and opens the study`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                hasCached = false,
                status = status(usedCount = 1),
            )
            start()

            // When
            viewModel.onEvent(DayStudyUiEvent.OnCardClick)

            // Then
            assertEquals(listOf(Triple(passages, dayRoute, LABEL)), coordinator.startedJobs)
            assertEquals(DayStudyGenerationUiModel(currentPhaseIndex = 0), viewModel.uiState.value.generation)
            assertEquals(listOf<DayStudyUiAction>(DayStudyUiAction.NavigateToStudy), actions)
            assertFalse(viewModel.uiState.value.isOpeningStudy)
            assertEquals(
                mapOf<String, Any>(
                    AnalyticsParams.PLAN_TYPE to "one_year",
                    AnalyticsParams.WEEK_NUMBER to 1,
                    AnalyticsParams.DAY_NUMBER to 1,
                    AnalyticsParams.IS_PRO to false,
                    AnalyticsParams.REMAINING_FREE to 2,
                ),
                trackedParams(AnalyticsEventNames.DAY_STUDY_GENERATION_STARTED),
            )
            assertEquals(
                mapOf<String, Any>(
                    AnalyticsParams.CARD_MODE to "generate",
                    AnalyticsParams.IS_PRO to false,
                    AnalyticsParams.SOURCE to "day_screen",
                ),
                trackedParams(AnalyticsEventNames.DAY_STUDY_CARD_CLICKED),
            )
        }

    @Test
    fun `GIVEN a signed out reader WHEN tapping generate THEN asks to sign in`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = false,
            status = status(usedCount = 0),
            userId = null,
        )
        start()

        // When
        viewModel.onEvent(DayStudyUiEvent.OnCardClick)

        // Then
        assertEquals(listOf<DayStudyUiAction>(DayStudyUiAction.NavigateToLoginWarning), actions)
        assertTrue(coordinator.startedJobs.isEmpty())
    }

    @Test
    fun `GIVEN no connection WHEN tapping generate THEN warns about being offline`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = false,
            status = status(usedCount = 0),
            isConnected = false,
        )
        start()

        // When
        viewModel.onEvent(DayStudyUiEvent.OnCardClick)

        // Then
        assertEquals(
            listOf<DayStudyUiAction>(DayStudyUiAction.ShowSnackBar(Res.string.ai_study_offline_message)),
            actions,
        )
        assertEquals(
            "offline",
            trackedParams(AnalyticsEventNames.DAY_STUDY_GENERATION_FAILED)?.get(AnalyticsParams.REASON),
        )
        assertTrue(coordinator.startedJobs.isEmpty())
    }

    @Test
    fun `GIVEN other generations using the free quota WHEN tapping generate THEN asks to wait for them`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                hasCached = false,
                status = status(usedCount = 2),
                generatingCount = 1,
            )
            start()

            // When
            viewModel.onEvent(DayStudyUiEvent.OnCardClick)

            // Then
            assertEquals(
                listOf<DayStudyUiAction>(
                    DayStudyUiAction.ShowSnackBarPlural(
                        resource = Res.plurals.ai_study_wait_for_generations,
                        count = 1,
                    ),
                ),
                actions,
            )
            assertTrue(coordinator.startedJobs.isEmpty())
        }

    @Test
    fun `GIVEN a pro reader WHEN tapping generate THEN starts regardless of other generations`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                hasCached = false,
                status = status(usedCount = 3),
                observeIsProUser = { flowOf(true) },
                generatingCount = 4,
            )
            start()

            // When
            viewModel.onEvent(DayStudyUiEvent.OnCardClick)

            // Then
            assertEquals(1, coordinator.startedJobs.size)
        }

    @Test
    fun `GIVEN an exhausted free quota WHEN tapping the card THEN opens the paywall`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = false,
            status = status(usedCount = 3),
        )
        start()

        // When
        viewModel.onEvent(DayStudyUiEvent.OnCardClick)

        // Then
        assertEquals(listOf<DayStudyUiAction>(DayStudyUiAction.NavigateToPaywall), actions)
        assertEquals(
            "locked",
            trackedParams(AnalyticsEventNames.DAY_STUDY_CARD_CLICKED)?.get(AnalyticsParams.CARD_MODE),
        )
    }

    @Test
    fun `GIVEN a cached study WHEN tapping view THEN opens the study`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = true,
            status = null,
        )
        start()

        // When
        viewModel.onEvent(DayStudyUiEvent.OnCardClick)

        // Then
        assertEquals(listOf<DayStudyUiAction>(DayStudyUiAction.NavigateToStudy), actions)
    }

    @Test
    fun `GIVEN a running generation WHEN tapping the card THEN opens the study to follow it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                hasCached = false,
                status = status(usedCount = 3),
            )
            start()
            coordinator.jobsFlow.value = listOf(
                job(
                    status = DayStudyGenerationStatus.Generating,
                    phase = null,
                ),
            )

            // When
            viewModel.onEvent(DayStudyUiEvent.OnCardClick)

            // Then
            assertEquals(listOf<DayStudyUiAction>(DayStudyUiAction.NavigateToStudy), actions)
        }

    @Test
    fun `GIVEN a running job WHEN it advances and then goes away THEN shows its step and then clears it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                hasCached = false,
                status = status(usedCount = 0),
            )
            start()

            // When
            coordinator.jobsFlow.value =
                listOf(
                    job(
                        status = DayStudyGenerationStatus.Generating,
                        phase = DayStudyPhaseModel.CHAPTERS,
                    ),
                )
            val generating = viewModel.uiState.value.generation
            coordinator.jobsFlow.value = emptyList()

            // Then
            assertEquals(DayStudyGenerationUiModel(currentPhaseIndex = 1), generating)
            assertNull(viewModel.uiState.value.generation)
        }

    @Test
    fun `GIVEN a running job WHEN it finishes THEN refreshes the card and clears the generation`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                hasCached = false,
                status = status(usedCount = 0),
            )
            start()
            coordinator.jobsFlow.value = listOf(
                job(
                    status = DayStudyGenerationStatus.Generating,
                    phase = null,
                ),
            )
            repository.status = status(
                usedCount = 1,
                isUnlocked = true,
            )

            // When
            coordinator.jobsFlow.value =
                listOf(
                    job(
                        status = DayStudyGenerationStatus.Done(dayStudyModel),
                        phase = null,
                    ),
                )

            // Then
            assertNull(viewModel.uiState.value.generation)
            assertEquals(DayStudyCardMode.VIEW, card().mode)
        }

    @Test
    fun `GIVEN a job WHEN it hits the free limit THEN locks the card`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = false,
            status = status(usedCount = 1),
        )
        start()

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

        // Then
        assertEquals(
            DayStudyCardUiModel(
                mode = DayStudyCardMode.LOCKED,
                quota = Loadable.Loaded(
                    DayStudyCardQuotaUiModel(
                        remainingFree = 0,
                        freeLimit = 3,
                    ),
                ),
                isPro = false,
            ),
            card(),
        )
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `GIVEN a job WHEN it fails offline THEN warns about being offline`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = false,
            status = status(usedCount = 1),
        )
        start()

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

        // Then
        assertEquals(
            listOf<DayStudyUiAction>(DayStudyUiAction.ShowSnackBar(Res.string.ai_study_offline_message)),
            actions,
        )
        assertEquals(DayStudyCardMode.GENERATE, card().mode)
    }

    @Test
    fun `GIVEN a started day WHEN starting a different day THEN reloads the card for it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = false,
            status = status(usedCount = 0),
        )
        start()

        // When
        viewModel.onEvent(
            DayStudyUiEvent.OnStart(
                passages = passages,
                dayRoute = otherDayRoute,
                label = LABEL,
            ),
        )

        // Then
        assertEquals(listOf(jobKey, "ONE_YEAR|1|2"), coordinator.activatedKeys)
        assertEquals(2, trackedEvents.count { (name, _) -> name == AnalyticsEventNames.DAY_STUDY_LOAD })
    }

    @Test
    fun `GIVEN a started day WHEN starting the same day again THEN keeps the loaded card`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = false,
            status = status(usedCount = 0),
        )
        start()

        // When
        start()

        // Then
        assertEquals(DayStudyCardMode.GENERATE, card().mode)
        assertEquals(1, trackedEvents.count { (name, _) -> name == AnalyticsEventNames.DAY_STUDY_LOAD })
    }

    @Test
    fun `GIVEN a started day WHEN the card is disposed THEN releases the active job`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            hasCached = false,
            status = null,
        )
        start()

        // When
        viewModelStore.clear()

        // Then
        assertEquals(listOf(jobKey), coordinator.clearedKeys)
    }

    private fun start() {
        viewModel.onEvent(
            DayStudyUiEvent.OnStart(
                passages = passages,
                dayRoute = dayRoute,
                label = LABEL,
            ),
        )
    }

    private fun card(): DayStudyCardUiModel = requireNotNull(
        viewModel.uiState.value.card
            .valueOrNull(),
    )

    private fun trackedParams(name: String): Map<String, Any>? =
        trackedEvents.lastOrNull { (eventName, _) -> eventName == name }?.second

    private fun status(
        usedCount: Int,
        isUnlocked: Boolean = false,
    ): DayStudyStatusModel = DayStudyStatusModel(
        freeLimit = 3,
        usedCount = usedCount,
        isUnlocked = isUnlocked,
        cacheToken = "token",
    )

    private fun job(
        status: DayStudyGenerationStatus,
        phase: DayStudyPhaseModel?,
    ): DayStudyGenerationJob = DayStudyGenerationJob(
        key = jobKey,
        label = LABEL,
        dayRoute = dayRoute,
        phase = phase,
        status = status,
    )

    private fun TestScope.prepareScenario(
        hasCached: Boolean,
        status: DayStudyStatusModel?,
        statusError: Throwable? = null,
        observeIsProUser: ObserveIsProUser = ObserveIsProUser { flowOf(false) },
        userId: String? = "user-id",
        isConnected: Boolean = true,
        generatingCount: Int = 0,
    ) {
        trackedEvents = mutableListOf()
        actions = mutableListOf()
        coordinator = FakeDayStudyGenerationCoordinator().apply {
            this.generatingCount = generatingCount
        }
        repository = FakeDayStudyRepository(
            hasCached = hasCached,
            status = status,
            statusError = statusError,
            events = emptyList(),
        )
        val bibleRepository = FakeBibleRepository()
        val languageCodeMapper = LanguageCodeMapper()
        val getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) }
        viewModelStore = ViewModelStore()
        viewModel = ViewModelProvider.create(
            store = viewModelStore,
            factory = viewModelFactory {
                initializer {
                    DayStudyViewModel(
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
                        isConnected = { isConnected },
                        generationCoordinator = coordinator,
                        observeIsProUser = observeIsProUser,
                        observeAuthenticatedUserId = { flowOf(userId) },
                        cardUiModelFactory = DayStudyCardUiModelFactory(),
                        trackEvent = { name, params -> trackedEvents += name to params },
                    )
                }
            },
        )[DayStudyViewModel::class]
        backgroundScope.launch { viewModel.uiAction.collect { actions += it } }
    }

    private companion object {
        const val LABEL = "Gênesis 1"
    }
}
