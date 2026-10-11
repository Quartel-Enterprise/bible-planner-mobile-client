package com.quare.bibleplanner.feature.read.presentation.listening

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChapterListeningGateTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val chapter = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 3)
    private val requestKey = "chapter_listening|GEN|3"
    private val resultStore = StudyUnlockResultStore()
    private lateinit var gate: ChapterListeningGate
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var recordedUnlocks: MutableList<ChapterLocationModel>
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private var allowedCount = 0

    @Test
    fun `GIVEN an open chapter WHEN requesting it THEN lets it play`() = runTest(testDispatcher) {
        // Given
        prepareScenario(access = { ChapterListeningAccessModel.Open })

        // When
        gate.request(
            scope = backgroundScope,
            chapter = chapter,
            onAllowed = { allowedCount++ },
        )

        // Then
        assertEquals(1, allowedCount)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN a failing access check WHEN requesting THEN lets it play`() = runTest(testDispatcher) {
        // Given
        prepareScenario(access = { error("offline") })

        // When
        gate.request(
            scope = backgroundScope,
            chapter = chapter,
            onAllowed = { allowedCount++ },
        )

        // Then
        assertEquals(1, allowedCount)
    }

    @Test
    fun `GIVEN the daily unlock used WHEN requesting THEN opens the Pro teaser`() = runTest(testDispatcher) {
        // Given
        prepareScenario(access = { ChapterListeningAccessModel.LimitReached })

        // When
        gate.request(
            scope = backgroundScope,
            chapter = chapter,
            onAllowed = { allowedCount++ },
        )

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.Navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_LISTENING_LIMIT)),
            ),
            commands,
        )
        assertEquals(0, allowedCount)
    }

    @Test
    fun `GIVEN a signed-out user WHEN requesting THEN asks to log in first`() = runTest(testDispatcher) {
        // Given
        prepareScenario(access = { ChapterListeningAccessModel.LoginRequired })

        // When
        gate.request(
            scope = backgroundScope,
            chapter = chapter,
            onAllowed = { allowedCount++ },
        )

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.Navigate(LoginWarningNavRoute(LoginWarningReason.ChapterListening.key)),
            ),
            commands,
        )
        assertEquals(0, allowedCount)
    }

    @Test
    fun `GIVEN no video to offer and the free chapter unused WHEN requesting THEN unlocks it and plays`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                access = { createUnlockAvailable(isFreeUnlockAvailable = true) },
                isOfferPrepared = false,
            )

            // When
            gate.request(
                scope = backgroundScope,
                chapter = chapter,
                onAllowed = { allowedCount++ },
            )

            // Then
            assertEquals(listOf(chapter), recordedUnlocks)
            assertEquals(1, allowedCount)
            assertTrue(commands.isEmpty())
            assertEquals("chapter_listening_free_unlocked", trackedEvents.single().first)
        }

    @Test
    fun `GIVEN no video to offer and the free chapter used WHEN requesting THEN opens the Pro teaser`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                access = { createUnlockAvailable(isFreeUnlockAvailable = false) },
                isOfferPrepared = false,
                isRewardedUnlockEnabled = false,
            )

            // When
            gate.request(
                scope = backgroundScope,
                chapter = chapter,
                onAllowed = { allowedCount++ },
            )

            // Then
            assertEquals(
                listOf<NavigationCommand>(
                    NavigationCommand.Navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_LISTENING_FREE_LIMIT)),
                ),
                commands,
            )
            assertTrue(recordedUnlocks.isEmpty())
        }

    @Test
    fun `GIVEN no video and no free chapter configured WHEN the limit is reached THEN opens the Pro only teaser`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                access = { ChapterListeningAccessModel.LimitReached },
                isRewardedUnlockEnabled = false,
                freeDailyLimit = 0,
            )

            // When
            gate.request(
                scope = backgroundScope,
                chapter = chapter,
                onAllowed = { allowedCount++ },
            )

            // Then
            assertEquals(
                listOf<NavigationCommand>(
                    NavigationCommand.Navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_LISTENING_PRO_ONLY)),
                ),
                commands,
            )
        }

    @Test
    fun `GIVEN an unlock left WHEN requesting THEN offers the video for this chapter`() = runTest(testDispatcher) {
        // Given
        prepareScenario(access = { createUnlockAvailable(isFreeUnlockAvailable = true) })

        // When
        gate.request(
            scope = backgroundScope,
            chapter = chapter,
            onAllowed = { allowedCount++ },
        )

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.Navigate(
                    StudyUnlockNavRoute(
                        surface = StudyUnlockSurface.CHAPTER_LISTENING,
                        paywallSource = PaywallEntrySource.CHAPTER_LISTENING,
                        requestKey = requestKey,
                        rewardedRemainingToday = 1,
                        canUnlockWithoutVideo = true,
                    ),
                ),
            ),
            commands,
        )
        assertEquals(0, allowedCount)
    }

    @Test
    fun `GIVEN the video offered WHEN the reward is earned THEN records the unlock and plays`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(access = { createUnlockAvailable(isFreeUnlockAvailable = true) })
            gate.request(
                scope = backgroundScope,
                chapter = chapter,
                onAllowed = { allowedCount++ },
            )

            // When
            resultStore.publishEarned(requestKey)

            // Then
            assertEquals(listOf(chapter), recordedUnlocks)
            assertEquals(1, allowedCount)
            assertTrue(trackedEvents.none { (name, _) -> name == "chapter_listening_free_unlocked" })
        }

    @Test
    fun `GIVEN the video offered WHEN unlocked without a video in the sheet THEN tracks the free unlock`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(access = { createUnlockAvailable(isFreeUnlockAvailable = true) })
            gate.request(
                scope = backgroundScope,
                chapter = chapter,
                onAllowed = { allowedCount++ },
            )

            // When
            resultStore.publishUnlockedWithoutVideo(requestKey)

            // Then
            assertEquals(listOf(chapter), recordedUnlocks)
            assertEquals("chapter_listening_free_unlocked", trackedEvents.single().first)
        }

    private fun createUnlockAvailable(isFreeUnlockAvailable: Boolean): ChapterListeningAccessModel =
        ChapterListeningAccessModel.UnlockAvailable(
            rewardedRemainingToday = 1,
            isFreeUnlockAvailable = isFreeUnlockAvailable,
        )

    private fun TestScope.prepareScenario(
        access: suspend () -> ChapterListeningAccessModel,
        isOfferPrepared: Boolean = true,
        isRewardedUnlockEnabled: Boolean = true,
        freeDailyLimit: Int = 1,
    ) {
        val navigator = Navigator()
        commands = mutableListOf()
        recordedUnlocks = mutableListOf()
        trackedEvents = mutableListOf()
        allowedCount = 0
        gate = ChapterListeningGate(
            getChapterListeningAccess = { access() },
            recordChapterListeningUnlock = { unlocked -> recordedUnlocks += unlocked },
            prepareRewardedUnlockOffer = { isOfferPrepared },
            checkRewardedUnlockEnabled = { isRewardedUnlockEnabled },
            getFreeListeningDailyLimit = { freeDailyLimit },
            studyUnlockResultStore = resultStore,
            navigator = navigator,
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        backgroundScope.launch { navigator.commands.collect { command -> commands += command } }
    }
}
