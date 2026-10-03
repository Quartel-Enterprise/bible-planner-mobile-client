package com.quare.bibleplanner.feature.chapterstudy.presentation.viewmodel

import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationJob
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationStatus
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyPhaseModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyQuotaModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.PendingVerseFocusModel
import com.quare.bibleplanner.core.chapterstudy.domain.store.PendingVerseFocusStore
import com.quare.bibleplanner.core.chapterstudy.testing.FakeChapterStudyGenerationCoordinator
import com.quare.bibleplanner.core.chapterstudy.testing.createChapterStudy
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.ChatEntrySource
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.feature.chapterstudy.domain.usecase.ChapterStudyUseCases
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyContentUiState
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyHeroUiModel
import com.quare.bibleplanner.feature.chapterstudy.presentation.model.ChapterStudyUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChapterStudyViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val studyUnlockResultStore = StudyUnlockResultStore()
    private var isRewardedUnlockOffered = false
    private val target = ChapterStudyTargetModel(
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private val study: ChapterStudyModel = createChapterStudy()
    private val targetParams: Map<String, Any> = mapOf(
        "book_id" to "GEN",
        "chapter_number" to 3,
    )
    private val navigator = Navigator()
    private var isConnected = true
    private var accessChecks = 0
    private val freeQuota = ChapterStudyQuotaModel(
        freeLimit = 3,
        remainingFree = 2,
        isUnlocked = false,
        rewardedRemainingToday = 0,
    )
    private val usedUpQuota = freeQuota.copy(remainingFree = 0)
    private val commands = mutableListOf<NavigationCommand>()
    private lateinit var viewModel: ChapterStudyViewModel
    private lateinit var coordinator: FakeChapterStudyGenerationCoordinator
    private lateinit var pendingVerseFocusStore: PendingVerseFocusStore
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var refreshedTargets: MutableList<ChapterStudyTargetModel>
    private lateinit var keyVerseLookups: MutableList<List<Int>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a study on the device WHEN opening THEN shows it and checks it against the server`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(cachedStudy = study)

            // When
            settle()

            // Then
            assertEquals(
                expected = ChapterStudyContentUiState.Loaded(
                    study = study,
                    keyVerseText = KEY_VERSE_TEXT,
                ),
                actual = viewModel.uiState.value.content,
            )
            assertEquals(expected = listOf(listOf(15)), actual = keyVerseLookups)
            assertEquals(expected = listOf(target), actual = refreshedTargets)
            assertEquals(expected = emptyList(), actual = coordinator.startedTargets)
            assertEquals(
                expected = listOf("chapter_study_opened" to targetParams + ("is_cached" to true)),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN a study without a key verse WHEN opening THEN shows it without looking any text up`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(cachedStudy = study.copy(keyVerse = null))

            // When
            settle()

            // Then
            val content = viewModel.uiState.value.content as ChapterStudyContentUiState.Loaded
            assertNull(content.keyVerseText)
            assertEquals(expected = emptyList(), actual = keyVerseLookups)
        }

    @Test
    fun `GIVEN no study on the device WHEN opening online THEN starts the generation`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        settle()

        // Then
        assertEquals(
            expected = ChapterStudyContentUiState.Generating(currentPhaseIndex = 0),
            actual = viewModel.uiState.value.content,
        )
        assertEquals(expected = listOf(target), actual = coordinator.startedTargets)
        assertEquals(expected = emptyList(), actual = refreshedTargets)
        assertEquals(
            expected = listOf(
                "chapter_study_generation_started" to targetParams + ("is_pro" to false) + ("is_rewarded" to false),
            ),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN no study on the device WHEN opening offline THEN shows the connection error without generating`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isConnected = false)

            // When
            settle()

            // Then
            assertEquals(
                expected = ChapterStudyContentUiState.Failed(isOffline = true),
                actual = viewModel.uiState.value.content,
            )
            assertEquals(expected = emptyList(), actual = coordinator.startedTargets)
            assertEquals(
                expected = listOf(
                    "chapter_study_generation_failed" to targetParams + mapOf(
                        "reason" to "offline",
                        "is_pro" to false,
                    ),
                ),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN a generation already running WHEN opening THEN follows it instead of starting another`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                cachedStudy = study,
                job = job(
                    phase = ChapterStudyPhaseModel.CONTEXT,
                    status = ChapterStudyGenerationStatus.Generating,
                ),
            )

            // When
            settle()

            // Then
            assertEquals(
                expected = ChapterStudyContentUiState.Generating(currentPhaseIndex = 2),
                actual = viewModel.uiState.value.content,
            )
            assertEquals(expected = emptyList(), actual = coordinator.startedTargets)
            assertEquals(expected = emptyList(), actual = trackedEvents)
        }

    @Test
    fun `GIVEN a running generation WHEN it finishes THEN ticks every phase before showing the study`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            settle()
            trackedEvents.clear()

            // When
            coordinator.jobsFlow.value = listOf(
                job(
                    phase = ChapterStudyPhaseModel.QUESTIONS,
                    status = ChapterStudyGenerationStatus.Done(study),
                ),
            )
            runCurrent()
            val contentWhilePausing = viewModel.uiState.value.content
            settle()

            // Then
            assertEquals(
                expected = ChapterStudyContentUiState.Generating(currentPhaseIndex = 4),
                actual = contentWhilePausing,
            )
            assertEquals(
                expected = ChapterStudyContentUiState.Loaded(
                    study = study,
                    keyVerseText = KEY_VERSE_TEXT,
                ),
                actual = viewModel.uiState.value.content,
            )
            assertEquals(expected = listOf(target), actual = coordinator.acknowledgedTargets)
            assertEquals(
                expected = listOf("chapter_study_opened" to targetParams + ("is_cached" to false)),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN a running generation WHEN it fails THEN shows the error and lets the job go`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            settle()

            // When
            coordinator.jobsFlow.value = listOf(
                job(
                    phase = null,
                    status = ChapterStudyGenerationStatus.Failed(
                        isLimitReached = false,
                        isOffline = true,
                    ),
                ),
            )
            settle()

            // Then
            assertEquals(
                expected = ChapterStudyContentUiState.Failed(isOffline = true),
                actual = viewModel.uiState.value.content,
            )
            assertEquals(expected = listOf(target), actual = coordinator.acknowledgedTargets)
            assertEquals(expected = emptyList(), actual = commands)
        }

    @Test
    fun `GIVEN a running generation WHEN the free limit is hit THEN swaps the screen for the Pro teaser`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            settle()

            // When
            coordinator.jobsFlow.value = listOf(
                job(
                    phase = null,
                    status = ChapterStudyGenerationStatus.Failed(
                        isLimitReached = true,
                        isOffline = false,
                    ),
                ),
            )
            settle()

            // Then
            assertEquals(
                expected = listOf<NavigationCommand>(
                    NavigationCommand.NavigateReplacingTop(
                        PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_STUDY_LIMIT),
                    ),
                ),
                actual = commands,
            )
            assertEquals(expected = listOf(target), actual = coordinator.acknowledgedTargets)
        }

    @Test
    fun `GIVEN a study beside the reader with nothing generated WHEN opening THEN offers to generate it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isCompanion = true,
                quota = freeQuota,
            )

            // When
            settle()

            // Then
            assertEquals(
                expected = ChapterStudyContentUiState.NotGenerated(
                    hero = ChapterStudyHeroUiModel(
                        isPro = false,
                        quota = freeQuota,
                        isRewardedUnlockOffered = false,
                    ),
                    isStarting = false,
                ),
                actual = viewModel.uiState.value.content,
            )
            assertEquals(expected = emptyList(), actual = coordinator.startedTargets)
        }

    @Test
    fun `GIVEN a cached study beside the reader WHEN opening THEN shows it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            cachedStudy = study,
            isCompanion = true,
        )

        // When
        settle()

        // Then
        assertIs<ChapterStudyContentUiState.Loaded>(viewModel.uiState.value.content)
    }

    @Test
    fun `GIVEN the offer to generate WHEN generating with access THEN starts the generation`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isCompanion = true,
                quota = freeQuota,
            )
            settle()

            // When
            viewModel.onEvent(ChapterStudyUiEvent.OnGenerateClick)
            settle()

            // Then
            assertEquals(expected = listOf(target), actual = coordinator.startedTargets)
            assertEquals(
                expected = ChapterStudyContentUiState.Generating(currentPhaseIndex = 0),
                actual = viewModel.uiState.value.content,
            )
        }

    @Test
    fun `GIVEN a logged out user WHEN generating THEN asks to log in and keeps the offer`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            isCompanion = true,
            access = ChapterStudyAccessModel.LOGIN_REQUIRED,
        )
        settle()

        // When
        viewModel.onEvent(ChapterStudyUiEvent.OnGenerateClick)
        settle()

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(
                NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.ChapterStudy.key)),
            ),
            actual = commands,
        )
        assertIs<ChapterStudyContentUiState.NotGenerated>(viewModel.uiState.value.content)
        assertEquals(expected = emptyList(), actual = coordinator.startedTargets)
    }

    @Test
    fun `GIVEN the free studies used up WHEN generating THEN opens the Pro teaser without asking for access`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isCompanion = true,
                quota = usedUpQuota,
            )
            settle()

            // When
            viewModel.onEvent(ChapterStudyUiEvent.OnGenerateClick)
            settle()

            // Then
            assertEquals(
                expected = listOf<NavigationCommand>(
                    NavigationCommand.Navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_STUDY_LIMIT)),
                ),
                actual = commands,
            )
            assertEquals(expected = 0, actual = accessChecks)
        }

    @Test
    fun `GIVEN a rewarded video on offer WHEN generating with the free studies used up THEN opens the unlock sheet`() =
        runTest(testDispatcher) {
            // Given
            isRewardedUnlockOffered = true
            prepareScenario(
                isCompanion = true,
                quota = usedUpQuota.copy(rewardedRemainingToday = 2),
            )
            settle()

            // When
            viewModel.onEvent(ChapterStudyUiEvent.OnGenerateClick)
            settle()

            // Then
            assertEquals(
                expected = listOf<NavigationCommand>(
                    NavigationCommand.Navigate(
                        StudyUnlockNavRoute(
                            surface = StudyUnlockSurface.CHAPTER_STUDY,
                            paywallSource = PaywallEntrySource.CHAPTER_STUDY,
                            requestKey = REWARDED_REQUEST_KEY,
                            rewardedRemainingToday = 2,
                        ),
                    ),
                ),
                actual = commands,
            )
        }

    @Test
    fun `GIVEN the unlock sheet WHEN the reward is earned THEN starts a rewarded generation`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isCompanion = true,
                quota = usedUpQuota,
            )
            settle()

            // When
            studyUnlockResultStore.publishEarned(REWARDED_REQUEST_KEY)
            settle()

            // Then
            assertEquals(expected = listOf(target), actual = coordinator.startedTargets)
            assertEquals(expected = listOf(true), actual = coordinator.startedRewardFlags)
        }

    @Test
    fun `GIVEN an unserved reward WHEN generating the locked study THEN retries it without another video`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isCompanion = true,
                quota = usedUpQuota,
            )
            coordinator.unservedRewardTargets += target
            settle()

            // When
            viewModel.onEvent(ChapterStudyUiEvent.OnGenerateClick)
            settle()

            // Then
            assertEquals(expected = listOf(true), actual = coordinator.startedRewardFlags)
            assertEquals(expected = emptyList(), actual = commands)
        }

    @Test
    fun `GIVEN a rewarded video on offer WHEN a running generation hits the free limit THEN opens the unlock sheet`() =
        runTest(testDispatcher) {
            // Given
            isRewardedUnlockOffered = true
            prepareScenario(quota = usedUpQuota.copy(rewardedRemainingToday = 1))
            settle()

            // When
            coordinator.jobsFlow.value = listOf(
                job(
                    phase = null,
                    status = ChapterStudyGenerationStatus.Failed(
                        isLimitReached = true,
                        isOffline = false,
                    ),
                ),
            )
            settle()

            // Then
            assertEquals(
                expected = listOf<NavigationCommand>(
                    NavigationCommand.Navigate(
                        StudyUnlockNavRoute(
                            surface = StudyUnlockSurface.CHAPTER_STUDY,
                            paywallSource = PaywallEntrySource.CHAPTER_STUDY,
                            requestKey = REWARDED_REQUEST_KEY,
                            rewardedRemainingToday = 1,
                        ),
                    ),
                ),
                actual = commands,
            )
        }

    @Test
    fun `GIVEN access refused for the limit WHEN generating THEN opens the Pro teaser`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            isCompanion = true,
            quota = freeQuota,
            access = ChapterStudyAccessModel.LIMIT_REACHED,
        )
        settle()

        // When
        viewModel.onEvent(ChapterStudyUiEvent.OnGenerateClick)
        settle()

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(
                NavigationCommand.Navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_STUDY_LIMIT)),
            ),
            actual = commands,
        )
        assertEquals(expected = emptyList(), actual = coordinator.startedTargets)
    }

    @Test
    fun `GIVEN a study beside the reader WHEN generating hits the free limit THEN stays and offers Pro`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isCompanion = true,
                quota = usedUpQuota,
                job = job(
                    phase = null,
                    status = ChapterStudyGenerationStatus.Generating,
                ),
            )
            settle()

            // When
            coordinator.jobsFlow.value = listOf(
                job(
                    phase = null,
                    status = ChapterStudyGenerationStatus.Failed(
                        isLimitReached = true,
                        isOffline = false,
                    ),
                ),
            )
            settle()

            // Then
            assertEquals(expected = emptyList(), actual = commands)
            assertEquals(
                expected = ChapterStudyContentUiState.NotGenerated(
                    hero = ChapterStudyHeroUiModel(
                        isPro = false,
                        quota = usedUpQuota,
                        isRewardedUnlockOffered = false,
                    ),
                    isStarting = false,
                ),
                actual = viewModel.uiState.value.content,
            )
        }

    @Test
    fun `GIVEN a Pro user WHEN the account changes THEN the offer follows it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            isCompanion = true,
            isPro = true,
        )

        // When
        settle()

        // Then
        val content = assertIs<ChapterStudyContentUiState.NotGenerated>(viewModel.uiState.value.content)
        assertEquals(expected = true, actual = content.hero.isPro)
        assertEquals(expected = false, actual = content.hero.isLocked)
    }

    @Test
    fun `GIVEN a failed generation WHEN retrying THEN starts it again`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isConnected = false)
        settle()
        trackedEvents.clear()
        isConnected = true

        // When
        viewModel.onEvent(ChapterStudyUiEvent.OnRetryClick)
        settle()

        // Then
        assertEquals(expected = listOf(target), actual = coordinator.startedTargets)
        assertEquals(
            expected = ChapterStudyContentUiState.Generating(currentPhaseIndex = 0),
            actual = viewModel.uiState.value.content,
        )
        assertEquals(
            expected = listOf(
                "chapter_study_retry_clicked" to targetParams,
                "chapter_study_generation_started" to targetParams + ("is_pro" to false) + ("is_rewarded" to false),
            ),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN a study WHEN clicking an outline section THEN asks the reader below to show those verses`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(cachedStudy = study)
            settle()
            trackedEvents.clear()

            // When
            viewModel.onEvent(ChapterStudyUiEvent.OnOutlineSectionClick(study.outline.first()))
            settle()

            // Then
            assertEquals(
                expected = PendingVerseFocusModel(
                    bookId = BookId.GEN,
                    chapterNumber = 3,
                    verseNumbers = listOf(1, 2, 3, 4, 5, 6, 7),
                ),
                actual = pendingVerseFocusStore.pending.value,
            )
            assertEquals(expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack), actual = commands)
            assertEquals(expected = listOf("chapter_study_outline_clicked" to targetParams), actual = trackedEvents)
        }

    @Test
    fun `GIVEN a study beside the reader WHEN clicking an outline section THEN shows those verses and stays open`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(cachedStudy = study)
            settle()
            viewModel.onEvent(ChapterStudyUiEvent.OnWidthClassChanged(isWide = true))
            trackedEvents.clear()

            // When
            viewModel.onEvent(ChapterStudyUiEvent.OnOutlineSectionClick(study.outline.first()))
            settle()

            // Then
            assertEquals(
                expected = PendingVerseFocusModel(
                    bookId = BookId.GEN,
                    chapterNumber = 3,
                    verseNumbers = listOf(1, 2, 3, 4, 5, 6, 7),
                ),
                actual = pendingVerseFocusStore.pending.value,
            )
            assertEquals(expected = emptyList(), actual = commands)
            assertEquals(expected = listOf("chapter_study_outline_clicked" to targetParams), actual = trackedEvents)
        }

    @Test
    fun `GIVEN a study WHEN sharing the key verse THEN opens the share sheet for its verses`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(cachedStudy = study)
            settle()
            trackedEvents.clear()

            // When
            viewModel.onEvent(ChapterStudyUiEvent.OnShareKeyVerseClick)
            settle()

            // Then
            assertEquals(
                expected = listOf<NavigationCommand>(
                    NavigationCommand.Navigate(
                        ShareVerseNavRoute(
                            bookId = "GEN",
                            chapterNumber = 3,
                            verseNumbers = listOf(15),
                        ),
                    ),
                ),
                actual = commands,
            )
            assertEquals(
                expected = listOf("chapter_study_key_verse_share_clicked" to targetParams),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN no study on screen WHEN sharing the key verse THEN does nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isConnected = false)
        settle()
        trackedEvents.clear()

        // When
        viewModel.onEvent(ChapterStudyUiEvent.OnShareKeyVerseClick)
        settle()

        // Then
        assertEquals(expected = emptyList(), actual = commands)
        assertEquals(expected = emptyList(), actual = trackedEvents)
    }

    @Test
    fun `GIVEN a study WHEN clicking a cross reference THEN opens the reader on that passage`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(cachedStudy = study)
            settle()
            trackedEvents.clear()

            // When
            viewModel.onEvent(ChapterStudyUiEvent.OnCrossReferenceClick(study.crossReferences.first()))
            settle()

            // Then
            assertEquals(
                expected = listOf<NavigationCommand>(
                    NavigationCommand.Navigate(
                        ReadNavRoute(
                            bookId = "ROM",
                            chapterNumber = 5,
                            isChapterRead = true,
                            isFromBookDetails = false,
                            targetVerseNumbers = listOf(12, 13, 14, 15, 16, 17, 18, 19),
                        ),
                    ),
                ),
                actual = commands,
            )
            assertEquals(
                expected = listOf(
                    "chapter_study_cross_reference_clicked" to mapOf<String, Any>(
                        "book_id" to "ROM",
                        "chapter_number" to 5,
                    ),
                ),
                actual = trackedEvents,
            )
        }

    @Test
    fun `GIVEN a study WHEN clicking chat THEN opens the chat about this chapter`() = runTest(testDispatcher) {
        // Given
        prepareScenario(cachedStudy = study)
        settle()
        trackedEvents.clear()

        // When
        viewModel.onEvent(ChapterStudyUiEvent.OnAskAiClick)
        settle()

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(
                NavigationCommand.Navigate(
                    ChatNavRoute(
                        source = ChatEntrySource.CHAPTER_STUDY,
                        dayNumber = null,
                        weekNumber = null,
                        readingPlanType = null,
                        bookId = "GEN",
                        chapterNumber = 3,
                    ),
                ),
            ),
            actual = commands,
        )
        assertEquals(
            expected = listOf("ai_chat_entry_clicked" to mapOf<String, Any>("source" to "chapter_study")),
            actual = trackedEvents,
        )
    }

    private fun TestScope.settle() {
        advanceUntilIdle()
        runCurrent()
    }

    private fun job(
        phase: ChapterStudyPhaseModel?,
        status: ChapterStudyGenerationStatus,
    ): ChapterStudyGenerationJob = ChapterStudyGenerationJob(
        target = target,
        phase = phase,
        status = status,
    )

    private fun TestScope.prepareScenario(
        cachedStudy: ChapterStudyModel? = null,
        isConnected: Boolean = true,
        job: ChapterStudyGenerationJob? = null,
        isCompanion: Boolean = false,
        isPro: Boolean = false,
        quota: ChapterStudyQuotaModel? = null,
        access: ChapterStudyAccessModel = ChapterStudyAccessModel.OPEN,
    ) {
        this@ChapterStudyViewModelTest.isConnected = isConnected
        accessChecks = 0
        trackedEvents = mutableListOf()
        refreshedTargets = mutableListOf()
        keyVerseLookups = mutableListOf()
        pendingVerseFocusStore = PendingVerseFocusStore()
        coordinator = FakeChapterStudyGenerationCoordinator()
        coordinator.jobsFlow.value = listOfNotNull(job)
        backgroundScope.launch { navigator.commands.collect { commands += it } }
        viewModel = ChapterStudyViewModel(
            useCases = ChapterStudyUseCases(
                findCachedStudy = { cachedStudy },
                refreshCache = { refreshedTargets += it },
                getVersesShareContent = { _, _, verseNumbers ->
                    keyVerseLookups += verseNumbers
                    VersesShareContentModel(
                        text = KEY_VERSE_TEXT,
                        reference = "Genesis 3:15",
                        versionAbbreviation = "WEB",
                    )
                },
                isWholeChapterRead = { _, _ -> true },
                isConnected = { this@ChapterStudyViewModelTest.isConnected },
                observeIsProUser = { flowOf(isPro) },
                observeAuthenticatedUserId = { flowOf("user-1") },
                getAccess = {
                    accessChecks++
                    access
                },
                getQuota = { quota },
                prepareRewardedUnlockOffer = { isRewardedUnlockOffered },
            ),
            generationCoordinator = coordinator,
            pendingVerseFocusStore = pendingVerseFocusStore,
            studyUnlockResultStore = studyUnlockResultStore,
            navigator = navigator,
            route = ChapterStudyNavRoute(
                bookId = "GEN",
                chapterNumber = 3,
                isCompanion = isCompanion,
            ),
            platform = Platform.Android,
            trackEvent = { name, params -> trackedEvents += name to params },
        )
    }

    private companion object {
        const val REWARDED_REQUEST_KEY = "chapter_study|GEN|3"
        const val KEY_VERSE_TEXT = "I will put hostility between you and the woman."
    }
}
