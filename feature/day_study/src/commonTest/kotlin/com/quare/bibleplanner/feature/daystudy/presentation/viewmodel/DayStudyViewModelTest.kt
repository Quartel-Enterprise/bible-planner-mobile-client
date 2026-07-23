package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.daystudy.domain.coordinator.DayStudyGenerationCoordinatorImpl
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.feature.daystudy.presentation.factory.DayStudyCardUiModelFactory
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
internal class DayStudyViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a cached study and a pro flow that never emits WHEN starting THEN card loads as view immediately`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel(
                repository = FakeDayStudyRepository(hasCached = true, status = null),
                observeIsProUser = { emptyFlow() },
            )

            // When
            viewModel.onEvent(DayStudyUiEvent.OnStart(passages, dayRoute, LABEL))

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
            val viewModel = viewModel(
                repository = FakeDayStudyRepository(
                    hasCached = true,
                    status = DayStudyStatusModel(
                        freeLimit = 3,
                        usedCount = 1,
                        isUnlocked = true,
                        cacheToken = "token",
                    ),
                ),
                observeIsProUser = { flowOf(false) },
            )

            // When
            viewModel.onEvent(DayStudyUiEvent.OnStart(passages, dayRoute, LABEL))
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
        val viewModel = viewModel(
            repository = FakeDayStudyRepository(hasCached = true, status = null),
            observeIsProUser = { flowOf(false) },
        )

        // When
        viewModel.onEvent(DayStudyUiEvent.OnStart(passages, dayRoute, LABEL))
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
        val viewModel = viewModel(
            repository = FakeDayStudyRepository(hasCached = false, status = null),
            observeIsProUser = { flowOf(false) },
        )

        // When
        viewModel.onEvent(DayStudyUiEvent.OnStart(passages, dayRoute, LABEL))
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

    private fun TestScope.viewModel(
        repository: FakeDayStudyRepository,
        observeIsProUser: ObserveIsProUser,
    ): DayStudyViewModel {
        val bibleRepository = FakeBibleRepository()
        val languageCodeMapper = LanguageCodeMapper()
        val getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) }
        val getIntRemoteConfig = object : GetIntRemoteConfig {
            override suspend fun invoke(
                key: String,
                default: Int,
            ): Int = default
        }
        return DayStudyViewModel(
            getDayStudyQuota = GetDayStudyQuotaUseCase(
                repository = repository,
                bibleRepository = bibleRepository,
                getAppLanguageFlow = getAppLanguageFlow,
                languageCodeMapper = languageCodeMapper,
                getIntRemoteConfig = getIntRemoteConfig,
            ),
            hasCachedStudy = HasCachedStudyUseCase(
                repository = repository,
                bibleRepository = bibleRepository,
                getAppLanguageFlow = getAppLanguageFlow,
                languageCodeMapper = languageCodeMapper,
            ),
            isConnected = { true },
            generationCoordinator = DayStudyGenerationCoordinatorImpl(
                applicationScope = ApplicationScope(this),
                getDayStudy = GetDayStudyUseCase(
                    repository = repository,
                    bibleRepository = bibleRepository,
                    getAppLanguageFlow = getAppLanguageFlow,
                    languageCodeMapper = languageCodeMapper,
                ),
                observeIsProUser = observeIsProUser,
                networkConnectivityObserver = { MutableStateFlow(true) },
                isConnected = { true },
                trackEvent = { name, params -> trackedEvents += name to params },
            ),
            observeIsProUser = observeIsProUser,
            observeAuthenticatedUserId = { flowOf("user-id") },
            cardUiModelFactory = DayStudyCardUiModelFactory(),
            trackEvent = { name, params -> trackedEvents += name to params },
        )
    }

    private val passages = listOf(
        PassageModel(
            bookId = BookId.GEN,
            chapters = listOf(ChapterModel(number = 1, startVerse = null, endVerse = null, bookId = BookId.GEN)),
            isRead = false,
            chapterRanges = null,
        ),
    )
    private val dayRoute = DayNavRoute(dayNumber = 1, weekNumber = 1, readingPlanType = "ONE_YEAR")

    private companion object {
        const val LABEL = "Gênesis 1"
    }

    private class FakeDayStudyRepository(
        private val hasCached: Boolean,
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
        ): Boolean = hasCached
    }

    private class FakeBibleRepository : BibleRepository {
        override fun getBiblesFlow(): Flow<List<BibleModel>> = flowOf(emptyList())

        override fun getSelectedVersionIdFlow(): Flow<String> = flowOf("ACF")

        override suspend fun setSelectedVersionId(id: String) = Unit
    }
}
