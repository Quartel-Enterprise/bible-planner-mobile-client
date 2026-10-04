package com.quare.bibleplanner.feature.read.presentation.listening

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
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
    fun `GIVEN an unlock left but no ad to show WHEN requesting THEN opens the Pro teaser`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            access = { ChapterListeningAccessModel.UnlockAvailable(1) },
            isOfferPrepared = false,
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
                NavigationCommand.Navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_LISTENING_LIMIT)),
            ),
            commands,
        )
    }

    @Test
    fun `GIVEN an unlock left WHEN requesting THEN offers the video for this chapter`() = runTest(testDispatcher) {
        // Given
        prepareScenario(access = { ChapterListeningAccessModel.UnlockAvailable(1) })

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
            prepareScenario(access = { ChapterListeningAccessModel.UnlockAvailable(1) })
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
        }

    private fun TestScope.prepareScenario(
        access: suspend () -> ChapterListeningAccessModel,
        isOfferPrepared: Boolean = true,
    ) {
        val navigator = Navigator()
        commands = mutableListOf()
        recordedUnlocks = mutableListOf()
        allowedCount = 0
        gate = ChapterListeningGate(
            getChapterListeningAccess = { access() },
            recordChapterListeningUnlock = { unlocked -> recordedUnlocks += unlocked },
            prepareRewardedUnlockOffer = { isOfferPrepared },
            studyUnlockResultStore = resultStore,
            navigator = navigator,
        )
        backgroundScope.launch { navigator.commands.collect { command -> commands += command } }
    }
}
