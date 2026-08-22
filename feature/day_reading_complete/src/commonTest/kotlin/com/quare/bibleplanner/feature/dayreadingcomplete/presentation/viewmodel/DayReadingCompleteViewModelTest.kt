package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.viewmodel

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
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
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiAction
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiEvent
import com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model.DayReadingCompleteUiState
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyQuotaUseCase
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
private val testDay = DayModel(
    number = 1,
    passages = testPassages,
    isRead = true,
    totalVerses = 0,
    readVerses = 0,
    readTimestamp = null,
    plannedReadDate = testPlannedReadDate,
    notes = null,
    isToday = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
internal class DayReadingCompleteViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var actions: List<DayReadingCompleteUiAction>
    private lateinit var trackedEvents: MutableList<String>
    private lateinit var coordinator: FakeDayStudyGenerationCoordinator

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads a free user with quota as free with quota`() = runTest(testDispatcher) {
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1)
        runCurrent()

        val state = viewModel.uiState.value
        assertIs<DayReadingCompleteUiState.Loaded>(state)
        assertEquals(DayTimingState.ON_TIME, state.timing)
        assertEquals(StudyCtaState.FreeWithQuota(remaining = 2, limit = 3), state.ctaState)
    }

    @Test
    fun `loads a free user with no quota left as free exhausted`() = runTest(testDispatcher) {
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 3)
        runCurrent()

        val state = viewModel.uiState.value
        assertIs<DayReadingCompleteUiState.Loaded>(state)
        assertEquals(StudyCtaState.FreeExhausted(limit = 3), state.ctaState)
    }

    @Test
    fun `loads a pro user as pro regardless of quota`() = runTest(testDispatcher) {
        val viewModel = viewModel(isPro = true, freeLimit = 3, usedCount = 3)
        runCurrent()

        val state = viewModel.uiState.value
        assertIs<DayReadingCompleteUiState.Loaded>(state)
        assertEquals(StudyCtaState.Pro, state.ctaState)
    }

    @Test
    fun `stays loading when the day cannot be found`() = runTest(testDispatcher) {
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 0, day = null)
        runCurrent()

        assertEquals(DayReadingCompleteUiState.Loading, viewModel.uiState.value)
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `tapping the cta while exhausted opens the paywall`() = runTest(testDispatcher) {
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 3)
        runCurrent()

        viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
        runCurrent()

        assertEquals(
            expected = DayReadingCompleteUiAction.NavigateToRoute(
                route = PaywallNavRoute(PaywallEntrySource.DAY_STUDY),
                replace = false,
            ),
            actual = actions.last(),
        )
    }

    @Test
    fun `tapping the cta with quota left starts generation and opens the study`() = runTest(testDispatcher) {
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1)
        runCurrent()

        viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
        runCurrent()

        assertNotNull(coordinator.startedWith)
        assertEquals(
            expected = DayReadingCompleteUiAction.NavigateToRoute(
                route = DayStudyNavRoute(
                    dayNumber = testRoute.dayNumber,
                    weekNumber = testRoute.weekNumber,
                    readingPlanType = testRoute.readingPlanType,
                ),
                replace = true,
            ),
            actual = actions.last(),
        )
    }

    @Test
    fun `tapping the cta while logged out asks the reader to sign in first`() = runTest(testDispatcher) {
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1, isLoggedIn = false)
        runCurrent()

        viewModel.onEvent(DayReadingCompleteUiEvent.OnCtaClick("Gênesis 1-3"))
        runCurrent()

        assertNull(coordinator.startedWith)
        assertEquals(
            expected = DayReadingCompleteUiAction.NavigateToRoute(
                route = LoginWarningNavRoute(LoginWarningReason.DayStudy.key),
                replace = false,
            ),
            actual = actions.last(),
        )
    }

    @Test
    fun `dismissing navigates back`() = runTest(testDispatcher) {
        val viewModel = viewModel(isPro = false, freeLimit = 3, usedCount = 1)
        runCurrent()

        viewModel.onEvent(DayReadingCompleteUiEvent.OnDismiss)
        runCurrent()

        assertEquals(
            expected = DayReadingCompleteUiAction.NavigateBack,
            actual = actions.last(),
        )
        assertTrue(trackedEvents.contains("day_reading_complete_dismissed"))
    }

    private fun TestScope.viewModel(
        isPro: Boolean,
        freeLimit: Int,
        usedCount: Int,
        day: DayModel? = testDay,
        isLoggedIn: Boolean = true,
    ): DayReadingCompleteViewModel {
        trackedEvents = mutableListOf()
        coordinator = FakeDayStudyGenerationCoordinator()
        val dayStudyRepository = FakeDayStudyRepository(
            status = DayStudyStatusModel(
                freeLimit = freeLimit,
                usedCount = usedCount,
                isUnlocked = false,
                cacheToken = "token",
            ),
        )
        val bibleRepository = FakeBibleRepository()
        val getIntRemoteConfig = object : GetIntRemoteConfig {
            override suspend fun invoke(
                key: String,
                default: Int,
            ): Int = default
        }
        val viewModel = DayReadingCompleteViewModel(
            route = testRoute,
            getDay = { _, _, _ -> day },
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
            isConnected = { true },
            classifyDayTiming = ClassifyDayTimingUseCase(
                currentTimestampProvider = { 0L },
                localDateTimeProvider = { LocalDateTime(testPlannedReadDate, LocalTime(12, 0)) },
            ),
            resolveStudyCtaState = ResolveStudyCtaStateUseCase(),
            generationCoordinator = coordinator,
            trackEvent = { name, _ -> trackedEvents += name },
        )
        actions = mutableListOf<DayReadingCompleteUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        return viewModel
    }

    private class FakeDayStudyRepository(
        private val status: DayStudyStatusModel?,
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
        ): DayStudyStatusModel? = status

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
}
