package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.core.daystudy.domain.repository.DayStudyRepository
import com.quare.bibleplanner.core.daystudy.domain.store.DayStudyQuotaPrefetchStore
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.ScheduledDayModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ClassifyDayTimingUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ResolveStudyCtaStateUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteBannerUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteBannerUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class DayReadingCompleteBannerViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testLocation = PlanDayLocationModel(
        weekNumber = 2,
        dayNumber = 3,
        readingPlanType = ReadingPlanType.BOOKS,
    )
    private val testPlannedReadDate = LocalDate(2026, 8, 21)
    private val testPassages = listOf(
        PassageModel(
            bookId = BookId.GEN,
            chapters = (1..2).map { number ->
                ChapterModel(
                    number = number,
                    startVerse = null,
                    endVerse = null,
                    bookId = BookId.GEN,
                )
            },
            isRead = true,
            chapterRanges = "1-2",
        ),
    )
    private val testDay = ScheduledDayModel(
        number = 3,
        passages = testPassages,
        plannedReadDate = testPlannedReadDate,
    )
    private val testDayRoute = DayNavRoute(
        dayNumber = 3,
        weekNumber = 2,
        readingPlanType = ReadingPlanType.BOOKS.name,
    )
    private val navigator = Navigator()
    private lateinit var viewModel: DayReadingCompleteBannerViewModel
    private lateinit var actions: List<DayReadingCompleteBannerUiAction>
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var coordinator: FakeDayStudyGenerationCoordinator
    private lateinit var isProFlow: MutableStateFlow<Boolean>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a missing day WHEN loading THEN stays loading and tracks nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario(day = null)

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(DayReadingCompleteUiState.Loading, state)
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a free user with quota WHEN loading THEN shows the day with the free cta`() = runTest(testDispatcher) {
        // Given
        prepareScenario(usedCount = 1)

        // When
        val state = viewModel.uiState.value

        // Then
        assertIs<DayReadingCompleteUiState.Loaded>(state)
        assertEquals(DayTimingState.ON_TIME, state.timing)
        assertEquals(testPlannedReadDate, state.plannedReadDate)
        assertEquals(testPassages, state.passages)
        assertEquals(2, state.chapterCount)
        assertEquals(Language.PORTUGUESE_BRAZIL, state.language)
        assertEquals(
            StudyCtaState.FreeWithQuota(
                remaining = 2,
                limit = 3,
            ),
            state.ctaState.valueOrNull(),
        )
    }

    @Test
    fun `GIVEN a pending quota WHEN loading THEN shows the day with the cta still loading`() = runTest(testDispatcher) {
        // Given
        prepareScenario(quotaGate = CompletableDeferred())

        // When
        val state = viewModel.uiState.value

        // Then
        assertIs<DayReadingCompleteUiState.Loaded>(state)
        assertEquals(Loadable.Loading, state.ctaState)
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a prefetched quota and a pending fresh one WHEN loading THEN shows the prefetched cta first`() =
        runTest(testDispatcher) {
            // Given
            val quotaGate = CompletableDeferred<Unit>()
            prepareScenario(
                usedCount = 3,
                quotaGate = quotaGate,
                prefetchedQuota = DayStudyQuotaModel(
                    freeLimit = 3,
                    remainingFree = 1,
                    isUnlockedForDay = false,
                    hasLocalStudy = false,
                ),
            )
            val prefetchedState = viewModel.uiState.value

            // When
            quotaGate.complete(Unit)

            // Then
            assertIs<DayReadingCompleteUiState.Loaded>(prefetchedState)
            assertEquals(
                StudyCtaState.FreeWithQuota(
                    remaining = 1,
                    limit = 3,
                ),
                prefetchedState.ctaState.valueOrNull(),
            )
            val refreshedState = viewModel.uiState.value
            assertIs<DayReadingCompleteUiState.Loaded>(refreshedState)
            assertEquals(StudyCtaState.FreeExhausted(limit = 3), refreshedState.ctaState.valueOrNull())
        }

    @Test
    fun `GIVEN a loaded banner WHEN loading THEN tracks the shown event once with the day details`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(usedCount = 1)

            // When
            isProFlow.value = true

            // Then
            assertEquals(
                listOf(
                    "day_reading_complete_banner_shown" to mapOf<String, Any>(
                        "plan_type" to "books",
                        "week_number" to 2,
                        "day_number" to 3,
                        "timing" to "on_time",
                        "account_state" to "free",
                        "chapter_count" to 2,
                    ),
                ),
                trackedEvents,
            )
        }

    @Test
    fun `GIVEN a free user WHEN becoming pro THEN the cta turns pro`() = runTest(testDispatcher) {
        // Given
        prepareScenario(usedCount = 3)

        // When
        isProFlow.value = true

        // Then
        val state = viewModel.uiState.value
        assertIs<DayReadingCompleteUiState.Loaded>(state)
        assertEquals(StudyCtaState.Pro, state.ctaState.valueOrNull())
    }

    @Test
    fun `GIVEN an exhausted quota WHEN tapping the cta THEN opens the paywall and dismisses the banner`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(usedCount = 3)

            // When
            viewModel.onEvent(DayReadingCompleteBannerUiEvent.OnCtaClick("Gênesis 1-2"))

            // Then
            assertEquals(listOf(NavigationCommand.Navigate(PaywallNavRoute(PaywallEntrySource.DAY_STUDY))), commands)
            assertEquals(listOf(DayReadingCompleteBannerUiAction.Dismiss), actions)
            assertEquals(
                "day_reading_complete_banner_cta_clicked" to mapOf<String, Any>(
                    "account_state" to "free_exhausted",
                    "source" to "day_reading_complete_banner",
                ),
                trackedEvents.last(),
            )
            assertNull(coordinator.startedWith)
        }

    @Test
    fun `GIVEN quota left WHEN tapping the cta THEN starts generation opens the study and dismisses the banner`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(usedCount = 1)

            // When
            viewModel.onEvent(DayReadingCompleteBannerUiEvent.OnCtaClick("Gênesis 1-2"))

            // Then
            assertEquals(Triple(testPassages, testDayRoute, "Gênesis 1-2"), coordinator.startedWith)
            assertEquals(
                listOf(
                    NavigationCommand.Navigate(
                        DayStudyNavRoute(
                            dayNumber = 3,
                            weekNumber = 2,
                            readingPlanType = ReadingPlanType.BOOKS.name,
                        ),
                    ),
                ),
                commands,
            )
            assertEquals(listOf(DayReadingCompleteBannerUiAction.Dismiss), actions)
        }

    @Test
    fun `GIVEN a pro user WHEN tapping the cta THEN starts generation`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            usedCount = 3,
            isPro = true,
        )

        // When
        viewModel.onEvent(DayReadingCompleteBannerUiEvent.OnCtaClick("Gênesis 1-2"))

        // Then
        assertEquals(Triple(testPassages, testDayRoute, "Gênesis 1-2"), coordinator.startedWith)
        assertEquals("pro", trackedEvents.last().second.getValue("account_state"))
    }

    @Test
    fun `GIVEN no connection WHEN tapping the cta THEN shows the offline snackbar without starting`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                usedCount = 1,
                isConnected = false,
            )

            // When
            viewModel.onEvent(DayReadingCompleteBannerUiEvent.OnCtaClick("Gênesis 1-2"))

            // Then
            assertIs<DayReadingCompleteBannerUiAction.ShowSnackBar>(actions.single())
            assertNull(coordinator.startedWith)
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN a signed out reader WHEN tapping the cta THEN asks to sign in without starting`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                usedCount = 1,
                isLoggedIn = false,
            )

            // When
            viewModel.onEvent(DayReadingCompleteBannerUiEvent.OnCtaClick("Gênesis 1-2"))

            // Then
            assertEquals(
                listOf(NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.DayStudy.key))),
                commands,
            )
            assertNull(coordinator.startedWith)
            assertTrue(actions.isEmpty())
        }

    @Test
    fun `GIVEN the cta still loading WHEN tapping it THEN does nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario(quotaGate = CompletableDeferred())

        // When
        viewModel.onEvent(DayReadingCompleteBannerUiEvent.OnCtaClick("Gênesis 1-2"))

        // Then
        assertTrue(trackedEvents.isEmpty())
        assertTrue(commands.isEmpty())
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `GIVEN a loaded banner WHEN dismissing THEN dismisses and tracks it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(usedCount = 1)

        // When
        viewModel.onEvent(DayReadingCompleteBannerUiEvent.OnDismissClick)

        // Then
        assertEquals(listOf(DayReadingCompleteBannerUiAction.Dismiss), actions)
        assertEquals("day_reading_complete_banner_dismissed" to emptyMap(), trackedEvents.last())
    }

    private fun TestScope.prepareScenario(
        day: ScheduledDayModel? = testDay,
        usedCount: Int = 0,
        isPro: Boolean = false,
        isConnected: Boolean = true,
        isLoggedIn: Boolean = true,
        quotaGate: CompletableDeferred<Unit>? = null,
        prefetchedQuota: DayStudyQuotaModel? = null,
    ) {
        trackedEvents = mutableListOf()
        coordinator = FakeDayStudyGenerationCoordinator()
        isProFlow = MutableStateFlow(isPro)
        actions = mutableListOf()
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        val getIntRemoteConfig = object : GetIntRemoteConfig {
            override suspend fun invoke(
                key: String,
                default: Int,
            ): Int = default
        }
        viewModel = DayReadingCompleteBannerViewModel(
            day = testLocation,
            getScheduledDay = { _, _, _ -> day },
            getDayStudyQuota = GetDayStudyQuotaUseCase(
                repository = FakeDayStudyRepository(
                    status = DayStudyStatusModel(
                        freeLimit = 3,
                        usedCount = usedCount,
                        isUnlocked = false,
                        cacheToken = "token",
                    ),
                    statusGate = quotaGate,
                ),
                bibleRepository = FakeBibleRepository(),
                getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                languageCodeMapper = LanguageCodeMapper(),
                getIntRemoteConfig = getIntRemoteConfig,
            ),
            getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
            observeIsProUser = { isProFlow },
            observeAuthenticatedUserId = { flowOf(if (isLoggedIn) "user-id" else null) },
            isConnected = { isConnected },
            classifyDayTiming = ClassifyDayTimingUseCase(
                currentTimestampProvider = { 0L },
                localDateTimeProvider = { LocalDateTime(testPlannedReadDate, LocalTime(12, 0)) },
            ),
            resolveStudyCtaState = ResolveStudyCtaStateUseCase(),
            quotaPrefetchStore = DayStudyQuotaPrefetchStore().apply {
                prefetchedQuota?.let { quota ->
                    put(
                        day = testLocation,
                        quota = quota,
                    )
                }
            },
            generationCoordinator = coordinator,
            navigator = navigator,
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        actions = mutableListOf<DayReadingCompleteBannerUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}

private class FakeDayStudyRepository(
    private val status: DayStudyStatusModel?,
    private val statusGate: CompletableDeferred<Unit>?,
) : DayStudyRepository {
    override fun getDayStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Flow<DayStudyGenerationEventModel> = emptyFlow()

    override suspend fun getDayStudyStatus(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): DayStudyStatusModel? {
        statusGate?.await()
        return status
    }

    override suspend fun hasCachedStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Boolean = false
}

private class FakeBibleRepository : BibleRepository {
    override fun getBiblesFlow(): Flow<List<BibleModel>> = flowOf(emptyList())

    override fun getSelectedVersionIdFlow(): Flow<String> = flowOf("ACF")

    override suspend fun setSelectedVersionId(id: String) = Unit
}

private class FakeDayStudyGenerationCoordinator : DayStudyGenerationCoordinator {
    var startedWith: Triple<List<PassageModel>, DayNavRoute, String>? = null
    override val jobs: StateFlow<List<DayStudyGenerationJob>> = MutableStateFlow(emptyList())
    override val activeKey: StateFlow<String?> = MutableStateFlow(null)
    override val pendingOpenKey: StateFlow<String?> = MutableStateFlow(null)
    override val dismissedKeys: StateFlow<Set<String>> = MutableStateFlow(emptySet())

    override fun keyOf(dayRoute: DayNavRoute): String = "key"

    override fun start(
        passages: List<PassageModel>,
        dayRoute: DayNavRoute,
        label: String,
    ): String {
        startedWith = Triple(passages, dayRoute, label)
        return "key"
    }

    override fun setActive(key: String) = Unit

    override fun clearActive(key: String) = Unit

    override fun requestOpen(key: String) = Unit

    override fun consumePendingOpen(key: String) = Unit

    override fun dismissFromCard(key: String) = Unit

    override fun acknowledge(key: String) = Unit

    override fun getGeneratingCount(excludingKey: String?): Int = 0
}
