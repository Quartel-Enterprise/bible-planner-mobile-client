package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.core.daystudy.domain.store.DayStudyQuotaPrefetchStore
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.core.daystudy.testing.FakeDayStudyGenerationCoordinator
import com.quare.bibleplanner.core.daystudy.testing.FakeDayStudyRepository
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
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ClassifyDayTimingUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase.ResolveStudyCtaStateUseCase
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class DayReadingCompleteViewModelTest {
    private val testRoute = DayReadingCompleteNavRoute(
        dayNumber = 1,
        weekNumber = 1,
        readingPlanType = ReadingPlanType.CHRONOLOGICAL.name,
    )

    private val testPlannedReadDate = LocalDate(2026, 8, 21)

    private val testPassages = listOf(
        PassageModel(
            bookId = BookId.GEN,
            chapters = (1..3).map { number ->
                ChapterModel(number = number, startVerse = null, endVerse = null, bookId = BookId.GEN)
            },
            isRead = true,
            chapterRanges = "1-3",
        ),
    )

    private val testDay = ScheduledDayModel(
        number = 1,
        passages = testPassages,
        plannedReadDate = testPlannedReadDate,
    )

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var actions: List<DayReadingCompleteUiAction>
    private val navigator = Navigator()
    private val studyUnlockResultStore = StudyUnlockResultStore()
    private var isRewardedUnlockOffered = false
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var coordinator: FakeDayStudyGenerationCoordinator
    private lateinit var disabledSuggestions: MutableList<Boolean>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a free user with quota WHEN loading THEN shows free with quota`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1)

        // When
        runCurrent()

        // Then
        val state = viewModel.uiState.value
        assertIs<DayReadingCompleteUiState.Loaded>(state)
        assertEquals(DayTimingState.ON_TIME, state.timing)
        assertEquals(StudyCtaState.FreeWithQuota(remaining = 2, limit = 3), state.ctaState.valueOrNull())
    }

    @Test
    fun `GIVEN a free user with no quota left WHEN loading THEN shows free exhausted`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 3)

        // When
        runCurrent()

        // Then
        val state = viewModel.uiState.value
        assertIs<DayReadingCompleteUiState.Loaded>(state)
        assertEquals(
            expected = StudyCtaState.FreeExhausted(
                limit = 3,
                isRewardedUnlockOffered = false,
                rewardedRemainingToday = 2,
            ),
            actual = state.ctaState.valueOrNull(),
        )
    }

    @Test
    fun `GIVEN a pro user with no quota left WHEN loading THEN shows pro`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(isPro = true, freeLimit = 3, usedCount = 3)

        // When
        runCurrent()

        // Then
        val state = viewModel.uiState.value
        assertIs<DayReadingCompleteUiState.Loaded>(state)
        assertEquals(StudyCtaState.Pro, state.ctaState.valueOrNull())
    }

    @Test
    fun `GIVEN a day that cannot be found WHEN loading THEN stays loading`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 0, day = null)

        // When
        runCurrent()

        // Then
        assertEquals(DayReadingCompleteUiState.Loading, viewModel.uiState.value)
        assertTrue(actions.isEmpty())
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN an exhausted quota WHEN tapping the cta THEN opens the paywall`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 3)
        runCurrent()

        // When
        viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
        runCurrent()

        // Then
        assertEquals(
            expected = NavigationCommand.Navigate(PaywallNavRoute(PaywallEntrySource.DAY_STUDY)),
            actual = commands.last(),
        )
    }

    @Test
    fun `GIVEN an exhausted quota with a video on offer WHEN tapping the cta THEN opens the unlock sheet`() =
        runTest(testDispatcher) {
            // Given
            isRewardedUnlockOffered = true
            val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 3)
            runCurrent()

            // When
            viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
            runCurrent()

            // Then
            assertEquals(
                expected = NavigationCommand.Navigate(
                    StudyUnlockNavRoute(
                        surface = StudyUnlockSurface.DAY_READING_COMPLETE,
                        paywallSource = PaywallEntrySource.DAY_STUDY,
                        requestKey = SHEET_REQUEST_KEY,
                        rewardedRemainingToday = 2,
                    ),
                ),
                actual = commands.last(),
            )
        }

    @Test
    fun `GIVEN the unlock sheet open WHEN a reward is earned THEN starts a rewarded generation and opens the study`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 3)
            runCurrent()
            viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
            runCurrent()

            // When
            studyUnlockResultStore.publishEarned(SHEET_REQUEST_KEY)
            runCurrent()

            // Then
            assertEquals(listOf(true), coordinator.startedRewardFlags)
            assertEquals("Gênesis 1-3", coordinator.startedJobs.single().third)
            assertIs<NavigationCommand.NavigateReplacingTop>(commands.last())
        }

    @Test
    fun `GIVEN an unserved reward WHEN tapping the cta THEN retries it without another video`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 3)
            runCurrent()
            coordinator.unservedRewardKeys += SHEET_GENERATION_KEY

            // When
            viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
            runCurrent()

            // Then
            assertEquals(listOf(true), coordinator.startedRewardFlags)
        }

    @Test
    fun `GIVEN quota left WHEN tapping the cta THEN starts generation and opens the study`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1)
        runCurrent()

        // When
        viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
        runCurrent()

        // Then
        assertEquals(1, coordinator.startedJobs.size)
        assertEquals(
            expected = NavigationCommand.NavigateReplacingTop(
                DayStudyNavRoute(
                    dayNumber = testRoute.dayNumber,
                    weekNumber = testRoute.weekNumber,
                    readingPlanType = testRoute.readingPlanType,
                ),
            ),
            actual = commands.last(),
        )
    }

    @Test
    fun `GIVEN a logged out reader WHEN tapping the cta THEN asks the reader to sign in first`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1, isLoggedIn = false)
            runCurrent()

            // When
            viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
            runCurrent()

            // Then
            assertTrue(coordinator.startedJobs.isEmpty())
            assertEquals(
                expected = NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.DayStudy.key)),
                actual = commands.last(),
            )
        }

    @Test
    fun `GIVEN the quota still loading WHEN loading THEN shows the celebration without tracking`() =
        runTest(testDispatcher) {
            // Given
            val quotaGate = CompletableDeferred<Unit>()
            val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1, quotaGate = quotaGate)

            // When
            runCurrent()

            // Then
            val state = viewModel.uiState.value
            assertIs<DayReadingCompleteUiState.Loaded>(state)
            assertEquals(DayTimingState.ON_TIME, state.timing)
            assertEquals(Loadable.Loading, state.ctaState)
            assertTrue(trackedEvents.isEmpty())
        }

    @Test
    fun `GIVEN the celebration shown while the quota loads WHEN the quota answers THEN shows free with quota`() =
        runTest(testDispatcher) {
            // Given
            val quotaGate = CompletableDeferred<Unit>()
            val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1, quotaGate = quotaGate)
            runCurrent()

            // When
            quotaGate.complete(Unit)
            runCurrent()

            // Then
            val loadedState = viewModel.uiState.value
            assertIs<DayReadingCompleteUiState.Loaded>(loadedState)
            assertEquals(
                expected = StudyCtaState.FreeWithQuota(remaining = 2, limit = 3),
                actual = loadedState.ctaState.valueOrNull(),
            )
        }

    @Test
    fun `GIVEN a prefetched quota and a fresh one pending WHEN loading THEN shows the prefetched quota`() =
        runTest(testDispatcher) {
            // Given
            val quotaGate = CompletableDeferred<Unit>()
            val viewModel = viewModel(
                isPro = false,
                freeLimit = 3,
                usedCount = 3,
                quotaGate = quotaGate,
                prefetchedQuota = DayStudyQuotaModel(
                    freeLimit = 3,
                    remainingFree = 2,
                    isUnlockedForDay = false,
                    hasLocalStudy = false,
                    rewardedRemainingToday = 0,
                ),
            )

            // When
            runCurrent()

            // Then
            val state = viewModel.uiState.value
            assertIs<DayReadingCompleteUiState.Loaded>(state)
            assertEquals(
                expected = StudyCtaState.FreeWithQuota(remaining = 2, limit = 3),
                actual = state.ctaState.valueOrNull(),
            )
        }

    @Test
    fun `GIVEN a prefetched quota shown WHEN the fresh quota answers THEN shows the fresh quota`() =
        runTest(testDispatcher) {
            // Given
            val quotaGate = CompletableDeferred<Unit>()
            val viewModel = viewModel(
                isPro = false,
                freeLimit = 3,
                usedCount = 3,
                quotaGate = quotaGate,
                prefetchedQuota = DayStudyQuotaModel(
                    freeLimit = 3,
                    remainingFree = 2,
                    isUnlockedForDay = false,
                    hasLocalStudy = false,
                    rewardedRemainingToday = 0,
                ),
            )
            runCurrent()

            // When
            quotaGate.complete(Unit)
            runCurrent()

            // Then
            val refreshedState = viewModel.uiState.value
            assertIs<DayReadingCompleteUiState.Loaded>(refreshedState)
            assertEquals(
                expected = StudyCtaState.FreeExhausted(
                    limit = 3,
                    isRewardedUnlockOffered = false,
                    rewardedRemainingToday = 2,
                ),
                actual = refreshedState.ctaState.valueOrNull(),
            )
        }

    @Test
    fun `GIVEN a free user on time WHEN loading THEN tracks the shown event with the day timing and account state`() =
        runTest(testDispatcher) {
            // Given
            viewModel(isPro = false, freeLimit = 3, usedCount = 1)

            // When
            runCurrent()

            // Then
            assertEquals(
                expected = "day_reading_complete_shown" to mapOf<String, Any>(
                    "plan_type" to "chronological",
                    "week_number" to 1,
                    "day_number" to 1,
                    "timing" to "on_time",
                    "account_state" to "free",
                    "chapter_count" to 3,
                ),
                actual = trackedEvents.single { (name, _) -> name == "day_reading_complete_shown" },
            )
        }

    @Test
    fun `GIVEN the loaded sheet WHEN tapping never show again THEN disables the suggestion confirms and closes`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1)
            runCurrent()

            // When
            viewModel.onEvent(DayReadingCompleteUiEvent.OnNeverShowAgainClick)
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(false),
                actual = disabledSuggestions,
            )
            assertIs<DayReadingCompleteUiAction.ShowSnackBar>(actions.last())
            assertEquals(
                expected = NavigationCommand.NavigateBack,
                actual = commands.last(),
            )
        }

    @Test
    fun `GIVEN the loaded sheet WHEN dismissing THEN navigates back`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1)
        runCurrent()

        // When
        viewModel.onEvent(DayReadingCompleteUiEvent.OnDismiss)
        runCurrent()

        // Then
        assertEquals(
            expected = NavigationCommand.NavigateBack,
            actual = commands.last(),
        )
        assertTrue(trackedEvents.any { (name, _) -> name == "day_reading_complete_dismissed" })
    }

    @Test
    fun `GIVEN no connection WHEN tapping the cta THEN shows the offline snackbar without starting`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel(
                isPro = false,
                freeLimit = 3,
                usedCount = 1,
                isConnected = false,
            )
            runCurrent()

            // When
            viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
            runCurrent()

            // Then
            assertIs<DayReadingCompleteUiAction.ShowSnackBar>(actions.single())
            assertTrue(coordinator.startedJobs.isEmpty())
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN the cta still loading WHEN tapping it THEN does nothing`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel(
            isPro = false,
            freeLimit = 3,
            usedCount = 1,
            quotaGate = CompletableDeferred(),
        )
        runCurrent()

        // When
        viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
        runCurrent()

        // Then
        assertTrue(trackedEvents.isEmpty())
        assertTrue(commands.isEmpty())
        assertTrue(coordinator.startedJobs.isEmpty())
    }

    private fun TestScope.viewModel(
        isPro: Boolean,
        freeLimit: Int,
        usedCount: Int,
        day: ScheduledDayModel? = testDay,
        isLoggedIn: Boolean = true,
        isConnected: Boolean = true,
        quotaGate: CompletableDeferred<Unit>? = null,
        prefetchedQuota: DayStudyQuotaModel? = null,
    ): DayReadingCompleteViewModel {
        trackedEvents = mutableListOf()
        disabledSuggestions = mutableListOf()
        coordinator = FakeDayStudyGenerationCoordinator(pendingOpenKey = null)
        val dayStudyRepository = FakeDayStudyRepository(
            hasCached = false,
            status = DayStudyStatusModel(
                freeLimit = freeLimit,
                usedCount = usedCount,
                isUnlocked = false,
                cacheToken = "token",
                rewardedRemainingToday = 2,
            ),
            statusError = null,
            events = emptyList(),
        ).apply { statusGate = quotaGate }
        val bibleRepository = FakeBibleRepository(
            bibles = emptyList(),
            selectedVersionId = "ACF",
        )
        val getIntRemoteConfig = object : GetIntRemoteConfig {
            override suspend fun invoke(
                key: String,
                default: Int,
            ): Int = default
        }
        val viewModel = DayReadingCompleteViewModel(
            route = testRoute,
            getScheduledDay = { _, _, _ -> day },
            getDayStudyQuota = GetDayStudyQuotaUseCase(
                repository = dayStudyRepository,
                bibleRepository = bibleRepository,
                getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                languageCodeMapper = LanguageCodeMapper(),
                getIntRemoteConfig = getIntRemoteConfig,
            ),
            getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
            observeIsProUser = { flowOf(isPro) },
            observeAuthenticatedUserId = { flowOf(if (isLoggedIn) "user-id" else null) },
            isConnected = { isConnected },
            classifyDayTiming = ClassifyDayTimingUseCase(
                currentTimestampProvider = { 0L },
                localDateTimeProvider = { LocalDateTime(testPlannedReadDate, LocalTime(12, 0)) },
            ),
            resolveStudyCtaState = ResolveStudyCtaStateUseCase(
                prepareRewardedUnlockOffer = { isRewardedUnlockOffered },
            ),
            quotaPrefetchStore = DayStudyQuotaPrefetchStore().apply {
                prefetchedQuota?.let {
                    put(
                        day = PlanDayLocationModel(
                            weekNumber = testRoute.weekNumber,
                            dayNumber = testRoute.dayNumber,
                            readingPlanType = ReadingPlanType.valueOf(testRoute.readingPlanType),
                        ),
                        quota = it,
                    )
                }
            },
            generationCoordinator = coordinator,
            setStudySuggestionEnabled = { isEnabled -> disabledSuggestions += isEnabled },
            navigator = navigator,
            studyUnlockResultStore = studyUnlockResultStore,
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        actions = mutableListOf<DayReadingCompleteUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
        return viewModel
    }

    private companion object {
        const val SHEET_GENERATION_KEY = "CHRONOLOGICAL|1|1"
        const val SHEET_REQUEST_KEY = "day_reading_complete|$SHEET_GENERATION_KEY"
    }
}
