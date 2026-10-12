package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.createListeningSession
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toWholeChapterSegment
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class ListeningSleepTimerTest {
    private val tick = 1.seconds

    // The chapter runs to 10 s, and the tests sit at 4 s, 6 s before its end.
    private val session = createListeningSession(
        mode = ListeningModeModel.CHAPTER,
        day = null,
        segment = ChapterLocationModel(
            bookId = BookId.GEN,
            chapterNumber = 1,
        ).toWholeChapterSegment(),
        bibleVersionId = "ACF",
        shouldForceCanonOrder = false,
    ).copy(
        verses = listOf(
            createVerse(
                number = 1,
                start = Duration.ZERO,
                end = 4.seconds,
            ),
            createVerse(
                number = 2,
                start = 4.seconds,
                end = 10.seconds,
            ),
        ),
    )
    private val position = 4.seconds

    private lateinit var sleepTimer: ListeningSleepTimer
    private lateinit var store: ChapterListeningStateStore
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var emittedEvents: MutableList<ChapterListeningEventModel>
    private var elapsedCount = 0

    @Test
    fun `GIVEN a countdown on a playing chapter WHEN a second passes THEN counts a second down`() = runTest {
        // Given
        prepareScenario(timer = createCountdown(5.seconds))
        sleepTimer.ensureTicking(::onElapsed)

        // When
        advanceTimeBy(tick)
        runCurrent()

        // Then
        assertEquals(createCountdown(4.seconds), store.sleepTimer)
    }

    @Test
    fun `GIVEN a countdown on a paused chapter WHEN time passes THEN keeps the time left`() = runTest {
        // Given
        prepareScenario(
            timer = createCountdown(5.seconds),
            status = ListeningStatusModel.PAUSED,
        )
        sleepTimer.ensureTicking(::onElapsed)

        // When
        advanceTimeBy(tick * 3)
        runCurrent()

        // Then
        assertEquals(createCountdown(5.seconds), store.sleepTimer)
    }

    @Test
    fun `GIVEN a countdown about to end WHEN its time runs out THEN turns the timer off and reports it once`() =
        runTest {
            // Given
            prepareScenario(timer = createCountdown(2.seconds))
            sleepTimer.ensureTicking(::onElapsed)

            // When
            advanceTimeBy(tick * 5)
            runCurrent()

            // Then
            assertEquals(ListeningSleepTimerModel.Off, store.sleepTimer)
            assertEquals(1, elapsedCount)
        }

    @Test
    fun `GIVEN a countdown already ticking WHEN ensuring it ticks again THEN still counts a second per second`() =
        runTest {
            // Given
            prepareScenario(timer = createCountdown(5.seconds))
            sleepTimer.ensureTicking(::onElapsed)

            // When
            sleepTimer.ensureTicking(::onElapsed)
            advanceTimeBy(tick)
            runCurrent()

            // Then
            assertEquals(createCountdown(4.seconds), store.sleepTimer)
        }

    @Test
    fun `GIVEN no countdown when asked to tick WHEN a countdown is set later THEN it does not count down`() = runTest {
        // Given
        prepareScenario(timer = ListeningSleepTimerModel.Off)
        sleepTimer.ensureTicking(::onElapsed)

        // When
        store.update { it.copy(sleepTimer = createCountdown(5.seconds)) }
        advanceTimeBy(tick * 3)
        runCurrent()

        // Then
        assertEquals(createCountdown(5.seconds), store.sleepTimer)
    }

    @Test
    fun `GIVEN a ticking countdown WHEN cancelling it THEN stops counting down`() = runTest {
        // Given
        prepareScenario(timer = createCountdown(5.seconds))
        sleepTimer.ensureTicking(::onElapsed)

        // When
        sleepTimer.cancel()
        advanceTimeBy(tick * 3)
        runCurrent()

        // Then
        assertEquals(createCountdown(5.seconds), store.sleepTimer)
    }

    @Test
    fun `GIVEN no sleep timer WHEN checking the next segment THEN it will be reached`() = runTest {
        // Given
        prepareScenario(timer = ListeningSleepTimerModel.Off)

        // When
        val willReachNext = sleepTimer.willReachNextSegment(
            current = session,
            position = position,
        )

        // Then
        assertTrue(willReachNext)
    }

    @Test
    fun `GIVEN a timer for the end of the chapter WHEN checking the next segment THEN it will not be reached`() =
        runTest {
            // Given
            prepareScenario(timer = ListeningSleepTimerModel.EndOfChapter)

            // When
            val willReachNext = sleepTimer.willReachNextSegment(
                current = session,
                position = position,
            )

            // Then
            assertFalse(willReachNext)
        }

    @Test
    fun `GIVEN a countdown past the chapter end WHEN checking the next segment THEN it will be reached`() = runTest {
        // Given
        prepareScenario(timer = createCountdown(7.seconds))

        // When
        val willReachNext = sleepTimer.willReachNextSegment(
            current = session,
            position = position,
        )

        // Then
        assertTrue(willReachNext)
    }

    @Test
    fun `GIVEN a countdown before the chapter end WHEN checking the next segment THEN it will not be reached`() =
        runTest {
            // Given
            prepareScenario(timer = createCountdown(5.seconds))

            // When
            val willReachNext = sleepTimer.willReachNextSegment(
                current = session,
                position = position,
            )

            // Then
            assertFalse(willReachNext)
        }

    @Test
    fun `GIVEN a faster speed WHEN checking the next segment THEN the rest of the chapter takes less time`() = runTest {
        // Given
        prepareScenario(
            timer = createCountdown(5.seconds),
            speed = 2f,
        )

        // When
        val willReachNext = sleepTimer.willReachNextSegment(
            current = session,
            position = position,
        )

        // Then
        assertTrue(willReachNext)
    }

    @Test
    fun `GIVEN a sleep timer WHEN ending it THEN tracks its option and tells the listeners`() = runTest {
        // Given
        prepareScenario(timer = ListeningSleepTimerModel.EndOfChapter)

        // When
        sleepTimer.end("end_of_chapter")
        runCurrent()

        // Then
        assertEquals(
            listOf("chapter_listening_sleep_timer_ended" to mapOf<String, Any>("option" to "end_of_chapter")),
            trackedEvents,
        )
        assertEquals(listOf<ChapterListeningEventModel>(ChapterListeningEventModel.SleepTimerEnded), emittedEvents)
    }

    private fun onElapsed() {
        elapsedCount++
    }

    private fun createCountdown(remaining: Duration): ListeningSleepTimerModel = ListeningSleepTimerModel.Countdown(
        option = ListeningSleepTimerOption.FIFTEEN_MINUTES,
        remaining = remaining,
    )

    private fun createVerse(
        number: Int,
        start: Duration,
        end: Duration,
    ): ListeningVerseModel = ListeningVerseModel(
        number = number,
        text = "",
        start = start,
        end = end,
        words = emptyList(),
    )

    private fun TestScope.prepareScenario(
        timer: ListeningSleepTimerModel,
        status: ListeningStatusModel = ListeningStatusModel.PLAYING,
        speed: Float = 1f,
    ) {
        store = ChapterListeningStateStore()
        store.update { state ->
            state.copy(
                session = session.copy(status = status),
                settings = state.settings.copy(speed = speed),
                sleepTimer = timer,
            )
        }
        trackedEvents = mutableListOf()
        emittedEvents = mutableListOf()
        backgroundScope.launch { store.events.collect(emittedEvents::add) }
        runCurrent()
        sleepTimer = ListeningSleepTimer(
            scope = backgroundScope,
            store = store,
            tracker = ChapterListeningAnalyticsTracker { name, params -> trackedEvents += name to params },
        )
    }
}
