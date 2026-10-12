package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.createListeningSession
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toWholeChapterSegment
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.FindAdjacentListeningSegmentUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class ListeningPlaybackTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val positionTick = 200.milliseconds
    private val leadingSilence = 1.5.seconds
    private val genesisOne = createChapter(1)
    private val genesisTwo = createChapter(2)
    private val audio = createAudio(genesisOne)

    // Verse 1 runs from 0 s to 6 s, verse 2 from 6 s to 10 s with two timed words, and verse 3 to 14 s.
    private val verses = listOf(
        createVerse(
            number = 1,
            start = Duration.ZERO,
            end = 6.seconds,
            words = emptyList(),
        ),
        createVerse(
            number = 2,
            start = 6.seconds,
            end = 10.seconds,
            words = listOf(
                ListeningWordModel(
                    target = ListeningWordTargetModel.Verse(0 until 3),
                    start = 6.5.seconds,
                    end = 7.seconds,
                ),
                ListeningWordModel(
                    target = ListeningWordTargetModel.Verse(4 until 9),
                    start = 7.2.seconds,
                    end = 8.seconds,
                ),
            ),
        ),
        createVerse(
            number = 3,
            start = 10.seconds,
            end = 14.seconds,
            words = emptyList(),
        ),
    )

    private lateinit var playback: ListeningPlayback
    private lateinit var store: ChapterListeningStateStore
    private lateinit var audioPlayer: FakeChapterAudioPlayer
    private lateinit var audioRepository: FakeChapterAudioRepository
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var emittedEvents: MutableList<ChapterListeningEventModel>
    private var segmentEndCount = 0

    private val session: ListeningSessionModel?
        get() = store.session

    @Test
    fun `GIVEN the audio focus refused WHEN playing THEN stays paused without playing`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            status = ListeningStatusModel.PREPARING,
            canActivate = false,
        )

        // When
        playback.playFrom(Duration.ZERO)

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
        assertTrue(audioPlayer.plays.isEmpty())
    }

    @Test
    fun `GIVEN no audio loaded WHEN playing THEN nothing plays`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        playback.audio = null

        // When
        playback.playFrom(Duration.ZERO)

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
        assertTrue(audioPlayer.plays.isEmpty())
    }

    @Test
    fun `GIVEN a session without verses WHEN playing THEN nothing plays`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        store.updateSession { it.copy(verses = emptyList()) }

        // When
        playback.playFrom(Duration.ZERO)

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
        assertTrue(audioPlayer.plays.isEmpty())
    }

    @Test
    fun `GIVEN a loaded chapter WHEN playing from a word THEN plays the audio from there and shows the word`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            playback.playFrom(6.8.seconds)

            // Then
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
            assertEquals(audio.audioPath, audioPlayer.plays.single().audioPath)
            assertEquals(6.8.seconds, audioPlayer.plays.single().position)
            assertEquals(1f, audioPlayer.speed)
            assertEquals(1, session?.verseIndex)
            assertEquals(0, session?.wordIndex)
        }

    @Test
    fun `GIVEN a leading silence WHEN playing THEN shows it playing before the audio starts`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            playback.playFrom(
                start = Duration.ZERO,
                leadingSilence = leadingSilence,
            )

            // Then
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
            assertTrue(audioPlayer.plays.isEmpty())
        }

    @Test
    fun `GIVEN a leading silence WHEN the silence passes THEN starts the audio`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        playback.playFrom(
            start = Duration.ZERO,
            leadingSilence = leadingSilence,
        )

        // When
        advanceTimeBy(leadingSilence)
        runCurrent()

        // Then
        assertEquals(1, audioPlayer.plays.size)
    }

    @Test
    fun `GIVEN a chapter played and paused WHEN playing again THEN tracks the start only once`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            playback.playFrom(Duration.ZERO)
            playback.pause()

            // When
            playback.playFrom(playback.position)

            // Then
            assertEquals(1, trackedEvents.count { (name, _) -> name == "chapter_listening_started" })
        }

    @Test
    fun `GIVEN a chapter played and paused WHEN playing again THEN prefetches the next chapter only once`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            playback.playFrom(Duration.ZERO)
            playback.pause()

            // When
            playback.playFrom(playback.position)

            // Then
            assertEquals(listOf(genesisTwo), audioRepository.requests)
        }

    @Test
    fun `GIVEN auto next turned off WHEN playing a chapter THEN does not prefetch the next one`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isAutoNextEnabled = false)

            // When
            playback.playFrom(Duration.ZERO)

            // Then
            assertTrue(audioRepository.requests.isEmpty())
        }

    @Test
    fun `GIVEN a day reading with auto next turned off WHEN playing THEN still prefetches the next segment`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                mode = ListeningModeModel.DAY_READING,
                isAutoNextEnabled = false,
            )

            // When
            playback.playFrom(Duration.ZERO)

            // Then
            assertEquals(listOf(genesisTwo), audioRepository.requests)
        }

    @Test
    fun `GIVEN a sleep timer for the end of the chapter WHEN playing THEN does not prefetch the next one`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(sleepTimer = ListeningSleepTimerModel.EndOfChapter)

            // When
            playback.playFrom(Duration.ZERO)

            // Then
            assertTrue(audioRepository.requests.isEmpty())
        }

    @Test
    fun `GIVEN a playing chapter WHEN the audio moves on THEN follows its verse`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        playback.playFrom(Duration.ZERO)
        audioPlayer.position = 11.seconds

        // When
        advanceTimeBy(positionTick)
        runCurrent()

        // Then
        assertEquals(2, session?.verseIndex)
        assertEquals(11.seconds, playback.position)
    }

    @Test
    fun `GIVEN a playing segment WHEN the audio passes its last verse THEN stops and calls the segment end listener`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            playback.playFrom(Duration.ZERO)
            val pauseCount = audioPlayer.pauseCount
            audioPlayer.position = 14.seconds

            // When
            advanceTimeBy(positionTick)
            runCurrent()

            // Then
            assertEquals(1, segmentEndCount)
            assertEquals(pauseCount + 1, audioPlayer.pauseCount)
        }

    @Test
    fun `GIVEN a playing chapter WHEN its playback ends THEN calls the segment end listener`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            playback.playFrom(Duration.ZERO)
            val playbackId = audioPlayer.plays.last().playbackId

            // When
            playback.reachEnd(playbackId)

            // Then
            assertEquals(1, segmentEndCount)
        }

    @Test
    fun `GIVEN a playback stopped WHEN its old playback ends THEN ignores it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        playback.playFrom(Duration.ZERO)
        val oldPlaybackId = audioPlayer.plays.last().playbackId
        playback.pause()

        // When
        playback.reachEnd(oldPlaybackId)

        // Then
        assertEquals(0, segmentEndCount)
    }

    @Test
    fun `GIVEN a playing chapter WHEN pausing THEN pauses the player where it was`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        playback.playFrom(Duration.ZERO)
        val pauseCount = audioPlayer.pauseCount
        audioPlayer.position = 3.seconds

        // When
        playback.pause()

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
        assertEquals(pauseCount + 1, audioPlayer.pauseCount)
        assertEquals(3.seconds, playback.position)
    }

    @Test
    fun `GIVEN a paused chapter WHEN pausing THEN leaves the player alone`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        playback.pause()

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
        assertEquals(0, audioPlayer.pauseCount)
    }

    @Test
    fun `GIVEN an interrupted chapter WHEN pausing THEN pauses it for good`() = runTest(testDispatcher) {
        // Given
        prepareScenario(status = ListeningStatusModel.INTERRUPTED)

        // When
        playback.pause()

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
    }

    @Test
    fun `GIVEN a playing chapter WHEN interrupting it THEN shows it interrupted without pausing the player`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            playback.playFrom(Duration.ZERO)
            val pauseCount = audioPlayer.pauseCount

            // When
            playback.interrupt()

            // Then
            assertEquals(ListeningStatusModel.INTERRUPTED, session?.status)
            assertEquals(pauseCount, audioPlayer.pauseCount)
            assertEquals("chapter_listening_interrupted", trackedEvents.last().first)
        }

    @Test
    fun `GIVEN a paused chapter WHEN interrupting it THEN keeps it paused without tracking`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            playback.interrupt()

            // Then
            assertEquals(ListeningStatusModel.PAUSED, session?.status)
            assertTrue(trackedEvents.isEmpty())
        }

    @Test
    fun `GIVEN a finished chapter WHEN seeking while paused THEN shows the new position as paused`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(status = ListeningStatusModel.FINISHED)

            // When
            playback.seekPaused(7.5.seconds)

            // Then
            assertEquals(ListeningStatusModel.PAUSED, session?.status)
            assertEquals(1, session?.verseIndex)
            assertEquals(listOf(7.5.seconds), audioPlayer.seeks)
            assertEquals(7.5.seconds, playback.position)
        }

    @Test
    fun `GIVEN an interrupted chapter WHEN seeking while paused THEN keeps it interrupted`() = runTest(testDispatcher) {
        // Given
        prepareScenario(status = ListeningStatusModel.INTERRUPTED)

        // When
        playback.seekPaused(7.5.seconds)

        // Then
        assertEquals(ListeningStatusModel.INTERRUPTED, session?.status)
    }

    @Test
    fun `GIVEN a finished chapter WHEN showing a verse THEN shows it from its start as paused`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(status = ListeningStatusModel.FINISHED)
            store.updateSession {
                it.copy(
                    verseIndex = 1,
                    verseProgress = 0.5f,
                    wordIndex = 1,
                )
            }

            // When
            playback.showPausedVerse(
                verseIndex = 2,
                start = 10.seconds,
            )

            // Then
            assertEquals(ListeningStatusModel.PAUSED, session?.status)
            assertEquals(2, session?.verseIndex)
            assertEquals(0f, session?.verseProgress)
            assertNull(session?.wordIndex)
            assertEquals(listOf(10.seconds), audioPlayer.seeks)
            assertEquals(10.seconds, playback.position)
        }

    @Test
    fun `GIVEN a playing chapter WHEN finishing it THEN shows it complete and rewinds to its first verse`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            playback.playFrom(7.seconds)
            audioPlayer.position = 9.seconds

            // When
            playback.finish(genesisOne)

            // Then
            assertEquals(ListeningStatusModel.FINISHED, session?.status)
            assertEquals(2, session?.verseIndex)
            assertEquals(1f, session?.verseProgress)
            assertNull(session?.wordIndex)
            assertEquals(genesisOne, session?.finishedChapter)
            assertEquals(Duration.ZERO, playback.position)
        }

    @Test
    fun `GIVEN a playing chapter WHEN changing the speed THEN the player plays at it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        playback.playFrom(Duration.ZERO)

        // When
        playback.setSpeed(1.5f)

        // Then
        assertEquals(1.5f, audioPlayer.speed)
    }

    @Test
    fun `GIVEN a paused chapter WHEN changing the speed THEN leaves the player alone`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        playback.playFrom(Duration.ZERO)
        playback.pause()

        // When
        playback.setSpeed(2f)

        // Then
        assertEquals(1f, audioPlayer.speed)
    }

    @Test
    fun `GIVEN a countdown on a playing chapter WHEN it runs out THEN pauses and reports the sleep timer ended`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                sleepTimer = ListeningSleepTimerModel.Countdown(
                    option = ListeningSleepTimerOption.FIFTEEN_MINUTES,
                    remaining = 1.seconds,
                ),
            )
            playback.playFrom(Duration.ZERO)

            // When
            advanceTimeBy(1.seconds)
            runCurrent()

            // Then
            assertEquals(ListeningStatusModel.PAUSED, session?.status)
            assertEquals(listOf<ChapterListeningEventModel>(ChapterListeningEventModel.SleepTimerEnded), emittedEvents)
            assertEquals(
                "chapter_listening_sleep_timer_ended" to mapOf<String, Any>("option" to "countdown"),
                trackedEvents.last(),
            )
        }

    @Test
    fun `GIVEN a played chapter WHEN resetting the playback THEN forgets its audio and position`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            playback.playFrom(7.seconds)

            // When
            playback.reset()

            // Then
            assertNull(playback.audio)
            assertEquals(Duration.ZERO, playback.position)
        }

    @Test
    fun `GIVEN a played chapter reset WHEN playing it again THEN tracks a new start`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        playback.playFrom(Duration.ZERO)
        playback.reset()
        playback.audio = audio

        // When
        playback.playFrom(Duration.ZERO)

        // Then
        assertEquals(2, trackedEvents.count { (name, _) -> name == "chapter_listening_started" })
    }

    @Test
    fun `GIVEN a loaded chapter WHEN releasing the playback THEN releases the player and forgets the audio`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            playback.release()

            // Then
            assertEquals(1, audioPlayer.releaseCount)
            assertNull(playback.audio)
        }

    @Test
    fun `GIVEN a playing chapter WHEN the player reports events THEN passes on only those of the current playback`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val events = mutableListOf<ChapterAudioPlayerEvent>()
            backgroundScope.launch { playback.events.collect(events::add) }
            playback.playFrom(Duration.ZERO)
            val playbackId = audioPlayer.plays.last().playbackId

            // When
            audioPlayer.emit(ChapterAudioPlayerEvent.Ended(playbackId - 1))
            audioPlayer.emit(ChapterAudioPlayerEvent.Ended(playbackId))

            // Then
            assertEquals(listOf<ChapterAudioPlayerEvent>(ChapterAudioPlayerEvent.Ended(playbackId)), events)
        }

    @Test
    fun `GIVEN a supported player WHEN checking the playback THEN it is supported`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val isSupported = playback.isSupported

        // Then
        assertTrue(isSupported)
    }

    private fun onSegmentEnd() {
        segmentEndCount++
    }

    private fun createChapter(number: Int): ChapterLocationModel = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = number,
    )

    private fun createAudio(chapter: ChapterLocationModel): ChapterAudioModel = ChapterAudioModel(
        audioPath = "/cache/GEN_${chapter.chapterNumber}.mp3",
        titleWords = emptyList(),
        verses = emptyList(),
        duration = 14.seconds,
    )

    private fun createVerse(
        number: Int,
        start: Duration,
        end: Duration,
        words: List<ListeningWordModel>,
    ): ListeningVerseModel = ListeningVerseModel(
        number = number,
        text = "",
        start = start,
        end = end,
        words = words,
    )

    private fun TestScope.prepareScenario(
        status: ListeningStatusModel = ListeningStatusModel.PAUSED,
        canActivate: Boolean = true,
        mode: ListeningModeModel = ListeningModeModel.CHAPTER,
        isAutoNextEnabled: Boolean = true,
        sleepTimer: ListeningSleepTimerModel = ListeningSleepTimerModel.Off,
    ) {
        audioPlayer = FakeChapterAudioPlayer()
        audioRepository = FakeChapterAudioRepository(mapOf(genesisTwo to createAudio(genesisTwo)))
        store = ChapterListeningStateStore()
        store.update { state ->
            state.copy(
                session = createListeningSession(
                    mode = mode,
                    day = null,
                    segment = genesisOne.toWholeChapterSegment(),
                    bibleVersionId = "ACF",
                    shouldForceCanonOrder = false,
                ).copy(
                    verses = verses,
                    status = status,
                ),
                settings = state.settings.copy(isAutoNextEnabled = isAutoNextEnabled),
                sleepTimer = sleepTimer,
            )
        }
        trackedEvents = mutableListOf()
        emittedEvents = mutableListOf()
        backgroundScope.launch { store.events.collect(emittedEvents::add) }
        val tracker = ChapterListeningAnalyticsTracker { name, params -> trackedEvents += name to params }
        val readingOrder = listOf(genesisOne, genesisTwo)
        playback = ListeningPlayback(
            scope = backgroundScope,
            audioPlayer = audioPlayer,
            mediaSession = FakeListeningMediaSession(canActivate),
            store = store,
            sleepTimer = ListeningSleepTimer(
                scope = backgroundScope,
                store = store,
                tracker = tracker,
            ),
            prefetcher = ChapterAudioPrefetcher(
                scope = backgroundScope,
                chapterAudioRepository = audioRepository,
                findAdjacentSegment = FindAdjacentListeningSegmentUseCase { chapter, direction, _ ->
                    val step = if (direction == ChapterDirectionModel.NEXT) 1 else -1
                    readingOrder.getOrNull(readingOrder.indexOf(chapter) + step)
                },
                getChapterListeningAccess = { ChapterListeningAccessModel.Open },
            ),
            tracker = tracker,
        )
        playback.audio = audio
        playback.setSegmentEndListener(::onSegmentEnd)
    }
}
