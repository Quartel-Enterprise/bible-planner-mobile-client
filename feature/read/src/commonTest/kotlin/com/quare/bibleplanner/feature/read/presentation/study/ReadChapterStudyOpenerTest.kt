package com.quare.bibleplanner.feature.read.presentation.study

import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyQuotaModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.store.PendingVerseFocusStore
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyAccess
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyQuota
import com.quare.bibleplanner.core.chapterstudy.testing.FakeChapterStudyGenerationCoordinator
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.feature.read.domain.usecase.ReadStudyUseCases
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReadChapterStudyOpenerTest {
    private val target = ChapterStudyTargetModel(
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private val otherTarget = ChapterStudyTargetModel(
        bookId = BookId.EXO,
        chapterNumber = 1,
    )
    private val chapterStudyCommand: NavigationCommand = NavigationCommand.Navigate(
        ChapterStudyNavRoute(
            bookId = "GEN",
            chapterNumber = 3,
            isCompanion = false,
        ),
    )
    private val quotaWithTwoVideos = ChapterStudyQuotaModel(
        freeLimit = 3,
        remainingFree = 0,
        isUnlocked = false,
        rewardedRemainingToday = 2,
    )

    private lateinit var opener: ReadChapterStudyOpener
    private lateinit var chapterStudyCoordinator: FakeChapterStudyGenerationCoordinator
    private lateinit var studyUnlockResultStore: StudyUnlockResultStore
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: List<Pair<String, Map<String, Any>>>
    private lateinit var offeredRewardedRemainingCounts: List<Int>

    @Test
    fun `GIVEN an open study WHEN opening it THEN navigates to the study of the chapter`() = runTest {
        // Given
        prepareScenario(getChapterStudyAccess = { ChapterStudyAccessModel.OPEN })

        // When
        opener.open(
            scope = backgroundScope,
            target = target,
        )
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(chapterStudyCommand),
            actual = commands,
        )
        assertTrue(chapterStudyCoordinator.startedTargets.isEmpty())
    }

    @Test
    fun `GIVEN a signed out reader WHEN opening the study THEN warns that the study needs a login`() = runTest {
        // Given
        prepareScenario(getChapterStudyAccess = { ChapterStudyAccessModel.LOGIN_REQUIRED })

        // When
        opener.open(
            scope = backgroundScope,
            target = target,
        )
        runCurrent()

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(
                NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.ChapterStudy.key)),
            ),
            actual = commands,
        )
    }

    @Test
    fun `GIVEN the access check fails WHEN opening the study THEN opens the study anyway`() = runTest {
        // Given
        prepareScenario(getChapterStudyAccess = { error("offline") })

        // When
        opener.open(
            scope = backgroundScope,
            target = target,
        )
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(chapterStudyCommand),
            actual = commands,
        )
    }

    @Test
    fun `GIVEN an unserved reward WHEN opening a study over the limit THEN retries the rewarded study and opens it`() =
        runTest {
            // Given
            prepareScenario(
                getChapterStudyAccess = { ChapterStudyAccessModel.LIMIT_REACHED },
                unservedRewardTargets = setOf(target),
            )

            // When
            opener.open(
                scope = backgroundScope,
                target = target,
            )
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(target),
                actual = chapterStudyCoordinator.startedTargets,
            )
            assertEquals(
                expected = listOf(true),
                actual = chapterStudyCoordinator.startedRewardFlags,
            )
            assertEquals(
                expected = listOf(chapterStudyCommand),
                actual = commands,
            )
            assertTrue(offeredRewardedRemainingCounts.isEmpty())
        }

    @Test
    fun `GIVEN no video on offer WHEN opening a study over the limit THEN opens the paywall teaser`() = runTest {
        // Given
        prepareScenario(
            getChapterStudyAccess = { ChapterStudyAccessModel.LIMIT_REACHED },
            getChapterStudyQuota = { quotaWithTwoVideos },
        )

        // When
        opener.open(
            scope = backgroundScope,
            target = target,
        )
        runCurrent()

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(
                NavigationCommand.Navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_STUDY_LIMIT)),
            ),
            actual = commands,
        )
        assertEquals(
            expected = listOf(2),
            actual = offeredRewardedRemainingCounts,
        )
    }

    @Test
    fun `GIVEN the quota cannot be read WHEN opening a study over the limit THEN offers no remaining video`() =
        runTest {
            // Given
            prepareScenario(
                getChapterStudyAccess = { ChapterStudyAccessModel.LIMIT_REACHED },
                getChapterStudyQuota = { error("offline") },
            )

            // When
            opener.open(
                scope = backgroundScope,
                target = target,
            )
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(0),
                actual = offeredRewardedRemainingCounts,
            )
        }

    @Test
    fun `GIVEN a video on offer WHEN opening a study over the limit THEN opens the unlock sheet for the chapter`() =
        runTest {
            // Given
            prepareScenario(
                getChapterStudyAccess = { ChapterStudyAccessModel.LIMIT_REACHED },
                getChapterStudyQuota = { quotaWithTwoVideos },
                isRewardedUnlockOffered = true,
            )

            // When
            opener.open(
                scope = backgroundScope,
                target = target,
            )
            runCurrent()

            // Then
            assertEquals(
                expected = listOf<NavigationCommand>(
                    NavigationCommand.Navigate(
                        StudyUnlockNavRoute(
                            surface = StudyUnlockSurface.CHAPTER_STUDY,
                            paywallSource = PaywallEntrySource.CHAPTER_STUDY,
                            requestKey = REQUEST_KEY,
                            rewardedRemainingToday = 2,
                            canUnlockWithoutVideo = false,
                        ),
                    ),
                ),
                actual = commands,
            )
            assertTrue(chapterStudyCoordinator.startedTargets.isEmpty())
        }

    @Test
    fun `GIVEN the unlock sheet open WHEN the reward is earned THEN starts a rewarded study and opens it`() = runTest {
        // Given
        prepareScenario(
            getChapterStudyAccess = { ChapterStudyAccessModel.LIMIT_REACHED },
            isRewardedUnlockOffered = true,
        )
        opener.open(
            scope = backgroundScope,
            target = target,
        )
        runCurrent()

        // When
        studyUnlockResultStore.publishEarned(REQUEST_KEY)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(target),
            actual = chapterStudyCoordinator.startedTargets,
        )
        assertEquals(
            expected = listOf(true),
            actual = chapterStudyCoordinator.startedRewardFlags,
        )
        assertEquals(
            expected = chapterStudyCommand,
            actual = commands.last(),
        )
    }

    @Test
    fun `GIVEN the unlock sheet open WHEN the reward is earned THEN tracks the rewarded generation start`() = runTest {
        // Given
        prepareScenario(
            getChapterStudyAccess = { ChapterStudyAccessModel.LIMIT_REACHED },
            isRewardedUnlockOffered = true,
        )
        opener.open(
            scope = backgroundScope,
            target = target,
        )
        runCurrent()

        // When
        studyUnlockResultStore.publishEarned(REQUEST_KEY)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                AnalyticsEventNames.CHAPTER_STUDY_GENERATION_STARTED to mapOf<String, Any>(
                    AnalyticsParams.BOOK_ID to "GEN",
                    AnalyticsParams.CHAPTER_NUMBER to 3,
                    AnalyticsParams.IS_PRO to false,
                    AnalyticsParams.IS_REWARDED to true,
                ),
            ),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN the unlock sheet open WHEN a reward is earned for another request THEN starts nothing`() = runTest {
        // Given
        prepareScenario(
            getChapterStudyAccess = { ChapterStudyAccessModel.LIMIT_REACHED },
            isRewardedUnlockOffered = true,
        )
        opener.open(
            scope = backgroundScope,
            target = target,
        )
        runCurrent()

        // When
        studyUnlockResultStore.publishEarned(OTHER_REQUEST_KEY)
        runCurrent()

        // Then
        assertTrue(chapterStudyCoordinator.startedTargets.isEmpty())
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN the unlock sheet reopened for another chapter WHEN the first reward is earned THEN starts nothing`() =
        runTest {
            // Given
            prepareScenario(
                getChapterStudyAccess = { ChapterStudyAccessModel.LIMIT_REACHED },
                isRewardedUnlockOffered = true,
            )
            opener.open(
                scope = backgroundScope,
                target = target,
            )
            opener.open(
                scope = backgroundScope,
                target = otherTarget,
            )
            runCurrent()

            // When
            studyUnlockResultStore.publishEarned(REQUEST_KEY)
            runCurrent()

            // Then
            assertTrue(chapterStudyCoordinator.startedTargets.isEmpty())
        }

    private fun TestScope.prepareScenario(
        getChapterStudyAccess: GetChapterStudyAccess,
        getChapterStudyQuota: GetChapterStudyQuota = GetChapterStudyQuota { null },
        isRewardedUnlockOffered: Boolean = false,
        unservedRewardTargets: Set<ChapterStudyTargetModel> = emptySet(),
    ) {
        chapterStudyCoordinator = FakeChapterStudyGenerationCoordinator().apply {
            this.unservedRewardTargets += unservedRewardTargets
        }
        studyUnlockResultStore = StudyUnlockResultStore()
        val recordedOfferCounts = mutableListOf<Int>()
        offeredRewardedRemainingCounts = recordedOfferCounts
        val recordedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
        trackedEvents = recordedEvents
        val navigator = Navigator()
        opener = ReadChapterStudyOpener(
            studyUseCases = ReadStudyUseCases(
                prefetchDayStudyQuota = { error("unused") },
                prefetchChapterStudyStatus = { error("unused") },
                getChapterStudyAccess = getChapterStudyAccess,
                getChapterStudyQuota = getChapterStudyQuota,
                pendingVerseFocusStore = PendingVerseFocusStore(),
                chapterStudyGenerationCoordinator = chapterStudyCoordinator,
                prepareRewardedUnlockOffer = { rewardedRemainingToday ->
                    recordedOfferCounts += rewardedRemainingToday
                    isRewardedUnlockOffered
                },
                studyUnlockResultStore = studyUnlockResultStore,
            ),
            navigator = navigator,
            trackEvent = { name, params -> recordedEvents += name to params },
        )
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { command -> collected += command } }
        }
    }

    private companion object {
        const val REQUEST_KEY = "reader_chapter_study|GEN|3"
        const val OTHER_REQUEST_KEY = "reader_chapter_study|EXO|1"
    }
}
