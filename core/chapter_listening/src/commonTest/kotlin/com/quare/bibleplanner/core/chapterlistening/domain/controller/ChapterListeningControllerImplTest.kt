package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.books.domain.model.ChapterVerseContentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioFailureModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioWordModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningAllowanceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningSettingsRepository
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningTimeUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningWordsUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChapterListeningControllerImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val genesisOne = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 1)
    private val genesisTwo = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 2)
    private val genesisThree = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 3)
    private val versesByChapter = mapOf(
        genesisOne to mapOf(
            1 to "In the beginning God created the heaven and the earth.",
            2 to "And the earth was without form, and void.",
            3 to "And God said, Let there be light.",
        ),
        genesisTwo to mapOf(
            1 to "Thus the heavens and the earth were finished.",
            2 to "And on the seventh day God ended his work.",
        ),
        genesisThree to mapOf(1 to "Now the serpent was more subtil."),
    )
    private val headingsByChapter = mapOf(genesisOne to mapOf(2 to "The light"))

    // The title takes the first 2 s, a heading is read before verse 2, and both have their words timed with the
    // first words of verse 2.
    private val genesisOneAudio = ChapterAudioModel(
        audioPath = "/cache/GEN_1.mp3",
        titleWords = listOf(
            createAudioWord(
                offset = 0,
                start = 0.2.seconds,
                end = 0.8.seconds,
            ),
            createAudioWord(
                offset = 8,
                start = 0.9.seconds,
                end = 1.4.seconds,
            ),
        ),
        verses = listOf(
            createAudioVerse(number = 1, headingStart = null, start = 2.seconds, end = 5.5.seconds),
            createAudioVerse(
                number = 2,
                headingStart = 6.seconds,
                start = 7.seconds,
                end = 10.seconds,
                headingWords = listOf(
                    createAudioWord(
                        offset = 0,
                        start = 6.1.seconds,
                        end = 6.3.seconds,
                    ),
                    createAudioWord(
                        offset = 4,
                        start = 6.3.seconds,
                        end = 6.6.seconds,
                    ),
                ),
                words = listOf(
                    createAudioWord(
                        offset = 0,
                        start = 7.seconds,
                        end = 7.3.seconds,
                    ),
                    createAudioWord(
                        offset = 4,
                        start = 7.3.seconds,
                        end = 7.5.seconds,
                    ),
                    createAudioWord(
                        offset = 8,
                        start = 7.6.seconds,
                        end = 8.seconds,
                    ),
                ),
            ),
            createAudioVerse(number = 3, headingStart = null, start = 10.5.seconds, end = 13.seconds),
        ),
        duration = 14.seconds,
    )
    private val audioByChapter = mapOf(
        genesisOne to genesisOneAudio,
        genesisTwo to ChapterAudioModel(
            audioPath = "/cache/GEN_2.mp3",
            titleWords = emptyList(),
            verses = listOf(
                createAudioVerse(number = 1, headingStart = null, start = 2.seconds, end = 5.seconds),
                createAudioVerse(number = 2, headingStart = null, start = 5.5.seconds, end = 9.seconds),
            ),
            duration = 10.seconds,
        ),
        genesisThree to ChapterAudioModel(
            audioPath = "/cache/GEN_3.mp3",
            titleWords = emptyList(),
            verses = listOf(createAudioVerse(number = 1, headingStart = null, start = 2.seconds, end = 4.seconds)),
            duration = 5.seconds,
        ),
    )
    private val chapterGap = 1.5.seconds

    private lateinit var controller: ChapterListeningControllerImpl
    private lateinit var audioPlayer: FakeChapterAudioPlayer
    private lateinit var audioRepository: FakeChapterAudioRepository
    private lateinit var authenticatedUserId: MutableStateFlow<String?>
    private lateinit var mediaSession: FakeListeningMediaSession
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var emittedEvents: MutableList<ChapterListeningEventModel>

    private val session: ListeningSessionModel?
        get() = controller.state.value.session

    @Test
    fun `GIVEN a chapter WHEN starting it THEN plays its audio from the spoken title`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // Then
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
        assertEquals(versesByChapter.getValue(genesisOne).values.toList(), session?.verses?.map { it.text })
        assertEquals(listOf(Duration.ZERO, 6.seconds, 10.5.seconds), session?.verses?.map { it.start })
        assertEquals(listOf(6.seconds, 10.5.seconds, 14.seconds), session?.verses?.map { it.end })
        assertEquals(genesisOneAudio.audioPath, audioPlayer.plays.single().audioPath)
        assertEquals(Duration.ZERO, audioPlayer.plays.single().position)
        assertEquals("chapter_listening_started", trackedEvents.last().first)
    }

    @Test
    fun `GIVEN no connection and no cached audio WHEN starting THEN asks for a connection without playing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(failure = ChapterAudioFailureModel.NO_CONNECTION)

            // When
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // Then
            assertEquals(ListeningStatusModel.NEEDS_CONNECTION, session?.status)
            assertTrue(audioPlayer.plays.isEmpty())
            assertEquals("chapter_listening_audio_unavailable", trackedEvents.last().first)
            assertEquals("no_connection", trackedEvents.last().second["reason"])
        }

    @Test
    fun `GIVEN each audio failure WHEN starting THEN shows why it cannot play`() = runTest(testDispatcher) {
        // Given
        val expected = mapOf(
            ChapterAudioFailureModel.NOT_SIGNED_IN to ListeningStatusModel.NEEDS_SIGN_IN,
            ChapterAudioFailureModel.VERSION_NOT_ENABLED to ListeningStatusModel.VERSION_UNAVAILABLE,
            ChapterAudioFailureModel.BUDGET_REACHED to ListeningStatusModel.NARRATION_PAUSED,
            ChapterAudioFailureModel.UNAVAILABLE to ListeningStatusModel.AUDIO_UNAVAILABLE,
        )
        prepareScenario()

        // When
        val statuses = expected.keys.associateWith { failure ->
            audioRepository.failure = failure
            authenticatedUserId = MutableStateFlow("user-1")
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            session?.status
        }

        // Then
        assertEquals(expected, statuses)
    }

    @Test
    fun `GIVEN the connection came back WHEN playing again THEN loads and plays the chapter`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(failure = ChapterAudioFailureModel.NO_CONNECTION)
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            audioRepository.failure = null

            // When
            controller.togglePlayPause()

            // Then
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
            assertEquals(1, audioPlayer.plays.size)
        }

    @Test
    fun `GIVEN a chapter playing WHEN the audio reaches a verse THEN shows the verse and its progress`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            audioPlayer.position = 8.25.seconds
            advanceTimeBy(250.milliseconds)

            // Then
            assertEquals(1, session?.verseIndex)
            assertEquals(0.5f, session?.verseProgress)
        }

    @Test
    fun `GIVEN a chapter playing WHEN the audio reaches a word THEN marks the word being read`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            audioPlayer.position = 7.4.seconds
            advanceTimeBy(250.milliseconds)

            // Then
            assertEquals(ListeningWordTargetModel.Verse(4..6), spokenTarget())
        }

    @Test
    fun `GIVEN a chapter playing WHEN the audio reads the title THEN marks its chapter number`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            audioPlayer.position = 1.seconds
            advanceTimeBy(250.milliseconds)

            // Then
            assertEquals(ListeningWordTargetModel.ChapterNumber, spokenTarget())
        }

    @Test
    fun `GIVEN a chapter playing WHEN the audio reads a heading THEN marks its word`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        audioPlayer.position = 6.4.seconds
        advanceTimeBy(250.milliseconds)

        // Then
        assertEquals(ListeningWordTargetModel.Heading(4..8), spokenTarget())
    }

    @Test
    fun `GIVEN a chapter playing WHEN the audio is in the pause after a verse THEN marks no word`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            audioPlayer.position = 5.8.seconds
            advanceTimeBy(250.milliseconds)

            // Then
            assertEquals(0, session?.verseIndex)
            assertNull(session?.wordIndex)
        }

    @Test
    fun `GIVEN word highlight off WHEN the audio reaches a word THEN marks no word`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isWordHighlightEnabled = false)
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        audioPlayer.position = 7.4.seconds
        advanceTimeBy(250.milliseconds)

        // Then
        assertEquals(1, session?.verseIndex)
        assertNull(session?.wordIndex)
    }

    @Test
    fun `GIVEN a word being read WHEN the next word starts before the regular tick THEN moves to it on time`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            audioPlayer.position = 7.25.seconds
            advanceTimeBy(250.milliseconds)

            // When
            audioPlayer.position = 7.31.seconds
            advanceTimeBy(60.milliseconds)

            // Then
            assertEquals(ListeningWordTargetModel.Verse(4..6), spokenTarget())
        }

    @Test
    fun `GIVEN a paused chapter WHEN turning word highlight off and on THEN hides and shows the word right away`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            audioPlayer.position = 7.4.seconds
            advanceTimeBy(250.milliseconds)
            controller.togglePlayPause()

            // When
            controller.setWordHighlightEnabled(false)
            val wordWhenOff = session?.wordIndex
            controller.setWordHighlightEnabled(true)

            // Then
            assertNull(wordWhenOff)
            assertEquals(ListeningWordTargetModel.Verse(4..6), spokenTarget())
            assertEquals(true, settingsRepository.settings.value.isWordHighlightEnabled)
        }

    @Test
    fun `GIVEN a paused playback WHEN its late end arrives THEN ignores it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        val stalePlaybackId = audioPlayer.plays.last().playbackId
        controller.togglePlayPause()

        // When
        audioPlayer.emit(ChapterAudioPlayerEvent.Ended(stalePlaybackId))

        // Then
        assertEquals(genesisOne, session?.segment?.chapter)
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
    }

    @Test
    fun `GIVEN auto next on WHEN the chapter ends THEN plays the next one after a pause`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        audioPlayer.finish()
        val playsDuringGap = audioPlayer.plays.size
        advanceTimeBy(chapterGap + 1.milliseconds)

        // Then
        assertEquals(genesisTwo, session?.segment?.chapter)
        assertEquals(ChapterChangeCause.PLAYER, session?.chapterChangeCause)
        assertEquals(genesisOne, session?.finishedChapter)
        assertEquals(1, playsDuringGap)
        assertEquals(audioByChapter.getValue(genesisTwo).audioPath, audioPlayer.plays.last().audioPath)
        assertTrue(trackedEvents.any { (name, _) -> name == "chapter_listening_completed" })
    }

    @Test
    fun `GIVEN a pause while the next chapter is looked up WHEN the chapter ends THEN does not start the next one`() =
        runTest(testDispatcher) {
            // Given
            val accessGate = CompletableDeferred<Unit>()
            prepareScenario(accessGate = accessGate)
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            val playCount = audioPlayer.plays.size
            audioPlayer.finish()

            // When
            controller.togglePlayPause()
            accessGate.complete(Unit)
            advanceTimeBy(chapterGap + 1.milliseconds)

            // Then
            assertEquals(genesisTwo, session?.segment?.chapter)
            assertEquals(ListeningStatusModel.PAUSED, session?.status)
            assertEquals(playCount, audioPlayer.plays.size)
        }

    @Test
    fun `GIVEN a session that moved on WHEN moving to the chapter unlocked before THEN stays put`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            controller.moveToAdjacentChapter(
                direction = ChapterDirectionModel.NEXT,
                chapter = genesisThree,
                shouldPlay = true,
            )

            // Then
            assertEquals(genesisOne, session?.segment?.chapter)
        }

    @Test
    fun `GIVEN the unlocked next chapter WHEN moving to it THEN opens that chapter`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        controller.moveToAdjacentChapter(
            direction = ChapterDirectionModel.NEXT,
            chapter = genesisTwo,
            shouldPlay = false,
        )

        // Then
        assertEquals(genesisTwo, session?.segment?.chapter)
        assertEquals("/cache/GEN_2.mp3", audioPlayer.plays.last().audioPath)
    }

    @Test
    fun `GIVEN a chapter paused by the unlock video WHEN moving to the unlocked one THEN plays it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.togglePlayPause()

            // When
            controller.moveToAdjacentChapter(
                direction = ChapterDirectionModel.NEXT,
                chapter = genesisTwo,
                shouldPlay = true,
            )

            // Then
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
            assertEquals("/cache/GEN_2.mp3", audioPlayer.plays.last().audioPath)
        }

    @Test
    fun `GIVEN the monthly budget spent WHEN starting THEN shows the narration as paused`() = runTest(testDispatcher) {
        // Given
        prepareScenario(failure = ChapterAudioFailureModel.BUDGET_REACHED)

        // When
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // Then
        assertEquals(ListeningStatusModel.NARRATION_PAUSED, session?.status)
    }

    @Test
    fun `GIVEN a chapter paused while loading WHEN it loads THEN prefetches nothing until it plays`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val audioLoaded = CompletableDeferred<Unit>()
            audioRepository.gate = audioLoaded
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.togglePlayPause()

            // When
            audioLoaded.complete(Unit)

            // Then
            assertEquals(listOf(genesisOne), audioRepository.requests)
        }

    @Test
    fun `GIVEN auto next off WHEN the chapter ends THEN stops at its end`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isAutoNextEnabled = false)
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        audioPlayer.finish()

        // Then
        assertEquals(ListeningStatusModel.FINISHED, session?.status)
        assertEquals(genesisOne, session?.finishedChapter)
        assertEquals(1f, session?.verseProgress)
    }

    @Test
    fun `GIVEN a finished chapter WHEN playing again THEN plays it from the start`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isAutoNextEnabled = false)
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        audioPlayer.position = 14.seconds
        audioPlayer.finish()

        // When
        controller.togglePlayPause()

        // Then
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
        assertEquals(Duration.ZERO, audioPlayer.plays.last().position)
    }

    @Test
    fun `GIVEN the next chapter is locked WHEN the chapter ends THEN stops and offers it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(lockedChapters = setOf(genesisTwo))
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        audioPlayer.finish()

        // Then
        assertEquals(ListeningStatusModel.NEXT_LOCKED, session?.status)
        assertEquals(genesisTwo, session?.lockedSegment?.chapter)
        assertEquals(genesisOne, session?.segment?.chapter)
        assertEquals("chapter_listening_locked", trackedEvents.last().first)
    }

    @Test
    fun `GIVEN a locked chapter WHEN it is unlocked THEN plays it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(lockedChapters = setOf(genesisTwo))
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        audioPlayer.finish()

        // When
        controller.continueLockedChapter()

        // Then
        assertEquals(genesisTwo, session?.segment?.chapter)
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
        assertNull(session?.lockedSegment)
    }

    @Test
    fun `GIVEN a paused chapter WHEN resuming THEN continues from where it stopped`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        audioPlayer.position = 8.seconds
        controller.togglePlayPause()

        // When
        controller.togglePlayPause()

        // Then
        assertEquals(8.seconds, audioPlayer.plays.last().position)
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
    }

    @Test
    fun `GIVEN a chapter playing WHEN a call interrupts it THEN shows it as interrupted`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        mediaSession.interruptions.emit(AudioInterruptionModel.BEGAN)

        // Then
        assertEquals(ListeningStatusModel.INTERRUPTED, session?.status)
        assertEquals("chapter_listening_interrupted", trackedEvents.last().first)
    }

    @Test
    fun `GIVEN a chapter playing WHEN a call interrupts it THEN leaves the player to the system`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            val pauseCount = audioPlayer.pauseCount

            // When
            mediaSession.interruptions.emit(AudioInterruptionModel.BEGAN)

            // Then
            assertEquals(pauseCount, audioPlayer.pauseCount)
        }

    @Test
    fun `GIVEN an interrupted chapter WHEN opening another one THEN pauses the player`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        mediaSession.interruptions.emit(AudioInterruptionModel.BEGAN)
        val pauseCount = audioPlayer.pauseCount

        // When
        controller.nextChapter()

        // Then
        assertTrue(audioPlayer.pauseCount > pauseCount)
    }

    @Test
    fun `GIVEN an interrupted chapter WHEN the interruption ends THEN plays on`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        mediaSession.interruptions.emit(AudioInterruptionModel.BEGAN)

        // When
        mediaSession.interruptions.emit(AudioInterruptionModel.ENDED)

        // Then
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
    }

    @Test
    fun `GIVEN an interrupted chapter WHEN another app takes the audio for good THEN stays paused`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            mediaSession.interruptions.emit(AudioInterruptionModel.BEGAN)

            // When
            mediaSession.interruptions.emit(AudioInterruptionModel.PAUSE_REQUESTED)
            mediaSession.interruptions.emit(AudioInterruptionModel.ENDED)

            // Then
            assertEquals(ListeningStatusModel.PAUSED, session?.status)
            assertEquals(1, audioPlayer.plays.size)
        }

    @Test
    fun `GIVEN a paused chapter WHEN another interruption ends THEN stays paused`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        controller.togglePlayPause()

        // When
        mediaSession.interruptions.emit(AudioInterruptionModel.ENDED)

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
    }

    @Test
    fun `GIVEN a chapter still loading WHEN pausing THEN stays paused once it loads`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val audioLoaded = CompletableDeferred<Unit>()
        audioRepository.gate = audioLoaded
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        controller.togglePlayPause()
        audioLoaded.complete(Unit)

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
        assertEquals(3, session?.verses?.size)
        assertTrue(audioPlayer.plays.isEmpty())
    }

    @Test
    fun `GIVEN a chapter still loading WHEN a call interrupts it THEN stays interrupted once it loads`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val audioLoaded = CompletableDeferred<Unit>()
            audioRepository.gate = audioLoaded
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            mediaSession.interruptions.emit(AudioInterruptionModel.BEGAN)
            audioLoaded.complete(Unit)

            // Then
            assertEquals(ListeningStatusModel.INTERRUPTED, session?.status)
            assertTrue(audioPlayer.plays.isEmpty())
        }

    @Test
    fun `GIVEN a chapter paused while loading WHEN resuming before it loads THEN plays it once loaded`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val audioLoaded = CompletableDeferred<Unit>()
            audioRepository.gate = audioLoaded
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.togglePlayPause()

            // When
            controller.togglePlayPause()
            audioLoaded.complete(Unit)

            // Then
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
            assertEquals(1, audioPlayer.plays.size)
        }

    @Test
    fun `GIVEN a chapter playing WHEN it loads THEN prefetches the next one`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // Then
        assertEquals(listOf(genesisOne, genesisTwo), audioRepository.requests)
        assertTrue(audioRepository.refreshed.isEmpty())
    }

    @Test
    fun `GIVEN a locked next chapter WHEN a chapter loads THEN leaves the locked one alone`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(lockedChapters = setOf(genesisTwo))

            // When
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // Then
            assertEquals(listOf(genesisOne), audioRepository.requests)
        }

    @Test
    fun `GIVEN auto next off WHEN a chapter loads THEN prefetches nothing`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isAutoNextEnabled = false)

        // When
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // Then
        assertEquals(listOf(genesisOne), audioRepository.requests)
    }

    @Test
    fun `GIVEN a chapter playing WHEN the reader opens a locked chapter THEN keeps playing the current one`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(lockedChapters = setOf(genesisThree))
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            controller.followReader(genesisThree)

            // Then
            assertEquals(genesisOne, session?.segment?.chapter)
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
            assertNull(session?.lockedSegment)
        }

    @Test
    fun `GIVEN a locked next chapter WHEN the notification skips to it THEN keeps playing the current one`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(lockedChapters = setOf(genesisTwo))
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            mediaSession.commands.emit(ListeningRemoteCommand.NextChapter)

            // Then
            assertEquals(genesisOne, session?.segment?.chapter)
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
            assertEquals("chapter_listening_locked", trackedEvents.last().first)
        }

    @Test
    fun `GIVEN a paused chapter WHEN the reader opens another one THEN opens it paused without counting a start`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.togglePlayPause()
            val startedCount = trackedEvents.count { (name, _) -> name == "chapter_listening_started" }

            // When
            controller.followReader(genesisTwo)

            // Then
            assertEquals(ListeningStatusModel.PAUSED, session?.status)
            assertEquals(startedCount, trackedEvents.count { (name, _) -> name == "chapter_listening_started" })
        }

    @Test
    fun `GIVEN a chapter paused while loading WHEN it is played later THEN counts it as started once`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val audioLoaded = CompletableDeferred<Unit>()
            audioRepository.gate = audioLoaded
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.togglePlayPause()
            audioLoaded.complete(Unit)

            // When
            controller.togglePlayPause()
            controller.togglePlayPause()
            controller.togglePlayPause()

            // Then
            assertEquals(1, trackedEvents.count { (name, _) -> name == "chapter_listening_started" })
        }

    @Test
    fun `GIVEN a chapter still loading WHEN the reader opens a locked one THEN the load still finishes`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(lockedChapters = setOf(genesisTwo))
            val audioLoaded = CompletableDeferred<Unit>()
            audioRepository.gate = audioLoaded
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            controller.followReader(genesisTwo)
            audioLoaded.complete(Unit)

            // Then
            assertEquals(genesisOne, session?.segment?.chapter)
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
        }

    @Test
    fun `GIVEN a paused chapter WHEN the reader opens another one THEN follows it without loading its audio`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.togglePlayPause()

            // When
            controller.followReader(genesisThree)

            // Then
            assertEquals(genesisThree, session?.segment?.chapter)
            assertEquals(ListeningStatusModel.PAUSED, session?.status)
            assertFalse(genesisThree in audioRepository.requests)
            assertFalse(mediaSession.updates.last().isPlaying)
        }

    @Test
    fun `GIVEN a followed chapter never loaded WHEN playing THEN loads and plays it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        controller.togglePlayPause()
        controller.followReader(genesisThree)

        // When
        controller.togglePlayPause()

        // Then
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
        assertEquals("/cache/GEN_3.mp3", audioPlayer.plays.last().audioPath)
    }

    @Test
    fun `GIVEN the next chapter fails to prefetch WHEN a chapter loads THEN does not check it again`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            audioRepository.failingChapters = setOf(genesisTwo)

            // When
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // Then
            assertFalse(genesisTwo in audioRepository.refreshed)
        }

    @Test
    fun `GIVEN a chapter playing WHEN asking for its neighbours THEN follows the reading order`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisTwo,
                shouldForceCanonOrder = false,
            )

            // When
            val neighbours = listOf(
                controller.getAdjacentChapter(ChapterDirectionModel.PREVIOUS),
                controller.getAdjacentChapter(ChapterDirectionModel.NEXT),
            )

            // Then
            assertEquals(listOf(genesisOne, genesisThree), neighbours)
        }

    @Test
    fun `GIVEN nothing playing WHEN asking for neighbours THEN has none`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val next = controller.getAdjacentChapter(ChapterDirectionModel.NEXT)

        // Then
        assertNull(next)
    }

    @Test
    fun `GIVEN a chapter playing WHEN the headphones are unplugged THEN pauses`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        mediaSession.interruptions.emit(AudioInterruptionModel.PAUSE_REQUESTED)

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
    }

    @Test
    fun `GIVEN a chapter playing WHEN skipping to a verse THEN plays from it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        controller.skipToVerse(2)

        // Then
        assertEquals(2, session?.verseIndex)
        assertEquals(10.5.seconds, audioPlayer.plays.last().position)
    }

    @Test
    fun `GIVEN a chapter playing WHEN the lock screen scrubs to a position THEN plays from there`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            mediaSession.commands.emit(ListeningRemoteCommand.SeekTo(8.25.seconds))

            // Then
            assertEquals(8.25.seconds, audioPlayer.plays.last().position)
            assertEquals(1, session?.verseIndex)
            assertEquals(0.5f, session?.verseProgress)
        }

    @Test
    fun `GIVEN a paused chapter WHEN the lock screen scrubs past its end THEN moves there without playing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.togglePlayPause()
            val playCount = audioPlayer.plays.size

            // When
            mediaSession.commands.emit(ListeningRemoteCommand.SeekTo(1.minutes))
            controller.togglePlayPause()

            // Then
            assertEquals(playCount + 1, audioPlayer.plays.size)
            assertEquals(14.seconds, audioPlayer.plays.last().position)
        }

    @Test
    fun `GIVEN a paused chapter WHEN the lock screen scrubs THEN moves the player to show the new position`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.togglePlayPause()

            // When
            mediaSession.commands.emit(ListeningRemoteCommand.SeekTo(8.seconds))

            // Then
            assertEquals(listOf(8.seconds), audioPlayer.seeks)
        }

    @Test
    fun `GIVEN remote commands WHEN they arrive THEN drive the playback`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        mediaSession.commands.emit(ListeningRemoteCommand.Pause)
        mediaSession.commands.emit(ListeningRemoteCommand.NextVerse)
        mediaSession.commands.emit(ListeningRemoteCommand.PreviousVerse)
        mediaSession.commands.emit(ListeningRemoteCommand.Play)
        mediaSession.commands.emit(ListeningRemoteCommand.NextChapter)

        // Then
        assertEquals(genesisTwo, session?.segment?.chapter)
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
    }

    @Test
    fun `GIVEN the previous chapter command WHEN it arrives THEN plays the previous chapter`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisTwo,
                shouldForceCanonOrder = false,
            )

            // When
            mediaSession.commands.emit(ListeningRemoteCommand.PreviousChapter)

            // Then
            assertEquals(genesisOne, session?.segment?.chapter)
        }

    @Test
    fun `GIVEN the stop command WHEN it arrives THEN ends the session`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        mediaSession.commands.emit(ListeningRemoteCommand.Stop)

        // Then
        assertNull(session)
        assertEquals(1, mediaSession.deactivationCount)
        assertEquals(1, audioPlayer.releaseCount)
    }

    @Test
    fun `GIVEN half of a verse heard WHEN going to the previous verse THEN restarts the same verse`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            audioPlayer.position = 8.25.seconds
            advanceTimeBy(250.milliseconds)

            // When
            controller.previousVerse()

            // Then
            assertEquals(1, session?.verseIndex)
            assertEquals(6.seconds, audioPlayer.plays.last().position)
        }

    @Test
    fun `GIVEN a paused chapter WHEN skipping verses THEN moves without playing`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        controller.togglePlayPause()
        val playCount = audioPlayer.plays.size

        // When
        controller.skipToVerse(2)

        // Then
        assertEquals(2, session?.verseIndex)
        assertEquals(playCount, audioPlayer.plays.size)
    }

    @Test
    fun `GIVEN a paused chapter moved to a verse WHEN resuming THEN plays from that verse`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        controller.togglePlayPause()
        controller.skipToVerse(1)

        // When
        controller.togglePlayPause()

        // Then
        assertEquals(6.seconds, audioPlayer.plays.last().position)
    }

    @Test
    fun `GIVEN a chapter playing WHEN changing the speed THEN plays on at the new speed and saves it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            controller.setSpeed(1.5f)

            // Then
            assertEquals(1.5f, audioPlayer.speed)
            assertEquals(1.5f, settingsRepository.settings.value.speed)
        }

    @Test
    fun `GIVEN auto next WHEN turning it off THEN saves the choice`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        controller.setAutoNextEnabled(false)

        // Then
        assertEquals(false, settingsRepository.settings.value.isAutoNextEnabled)
    }

    @Test
    fun `GIVEN a sleep timer WHEN its time runs out while playing THEN pauses and says good night`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.setSleepTimer(ListeningSleepTimerOption.FIFTEEN_MINUTES)

            // When
            advanceTimeBy(15.minutes + 1.seconds)

            // Then
            assertEquals(ListeningStatusModel.PAUSED, session?.status)
            assertEquals(ListeningSleepTimerModel.Off, controller.state.value.sleepTimer)
            assertEquals(listOf<ChapterListeningEventModel>(ChapterListeningEventModel.SleepTimerEnded), emittedEvents)
        }

    @Test
    fun `GIVEN a paused chapter WHEN the sleep timer ticks THEN keeps its time`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        controller.setSleepTimer(ListeningSleepTimerOption.THIRTY_MINUTES)
        controller.togglePlayPause()

        // When
        advanceTimeBy(5.minutes)

        // Then
        assertEquals(
            ListeningSleepTimerModel.Countdown(
                option = ListeningSleepTimerOption.THIRTY_MINUTES,
                remaining = 30.minutes,
            ),
            controller.state.value.sleepTimer,
        )
    }

    @Test
    fun `GIVEN the end of chapter timer WHEN the chapter ends THEN stops there instead of moving on`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.setSleepTimer(ListeningSleepTimerOption.END_OF_CHAPTER)

            // When
            audioPlayer.finish()

            // Then
            assertEquals(ListeningStatusModel.FINISHED, session?.status)
            assertEquals(genesisOne, session?.segment?.chapter)
            assertEquals(listOf<ChapterListeningEventModel>(ChapterListeningEventModel.SleepTimerEnded), emittedEvents)
        }

    @Test
    fun `GIVEN a timer WHEN turning it off THEN clears it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.setSleepTimer(ListeningSleepTimerOption.END_OF_CHAPTER)

        // When
        controller.setSleepTimer(ListeningSleepTimerOption.OFF)

        // Then
        assertEquals(ListeningSleepTimerModel.Off, controller.state.value.sleepTimer)
    }

    @Test
    fun `GIVEN the last reader closed WHEN the grace passes THEN stops listening`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.attachReader()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        controller.detachReader()

        // When
        advanceTimeBy(3.seconds)

        // Then
        assertNull(session)
    }

    @Test
    fun `GIVEN a reader replaced by the next one WHEN the old one detaches THEN keeps listening`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.attachReader()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.detachReader()

            // When
            controller.attachReader()
            advanceTimeBy(3.seconds)

            // Then
            assertEquals(genesisOne, session?.segment?.chapter)
        }

    @Test
    fun `GIVEN a chapter playing WHEN the reader opens another chapter THEN plays that one`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            controller.followReader(genesisThree)

            // Then
            assertEquals(genesisThree, session?.segment?.chapter)
            assertEquals(ChapterChangeCause.READER, session?.chapterChangeCause)
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
        }

    @Test
    fun `GIVEN the reading of today WHEN a chapter ends THEN plays the next chapter of the day`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isAutoNextEnabled = false)
            controller.startDayReading(
                day = createDay(genesisOne, genesisThree),
                chapter = genesisOne,
            )

            // When
            audioPlayer.finish()

            // Then
            assertEquals(ListeningModeModel.DAY_READING, session?.mode)
            assertEquals(genesisThree, session?.segment?.chapter)
        }

    @Test
    fun `GIVEN the last chapter of the day WHEN it ends THEN finishes the reading`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startDayReading(
            day = createDay(genesisOne, genesisThree),
            chapter = genesisThree,
        )

        // When
        audioPlayer.finish()

        // Then
        assertEquals(ListeningStatusModel.FINISHED, session?.status)
        assertEquals(genesisThree, session?.finishedChapter)
    }

    @Test
    fun `GIVEN a partial chapter in the day WHEN playing it THEN starts at the heading of its first verse`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            controller.startDayReading(
                day = createPartialDay(
                    startVerse = 2,
                    endVerse = 3,
                ),
                chapter = genesisOne,
            )

            // Then
            assertEquals(listOf(2, 3), session?.verses?.map { it.number })
            assertEquals(6.seconds, audioPlayer.plays.single().position)
        }

    @Test
    fun `GIVEN a day segment from the first verse WHEN playing it THEN starts with the spoken title`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            controller.startDayReading(
                day = createPartialDay(
                    startVerse = 1,
                    endVerse = 2,
                ),
                chapter = genesisOne,
            )

            // Then
            assertEquals(Duration.ZERO, audioPlayer.plays.single().position)
        }

    @Test
    fun `GIVEN a chapter the gate unlocked but could not record WHEN moving to it THEN opens it anyway`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(lockedChapters = setOf(genesisTwo))
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            controller.moveToAdjacentChapter(
                direction = ChapterDirectionModel.NEXT,
                chapter = genesisTwo,
                shouldPlay = true,
            )

            // Then
            assertEquals(genesisTwo, session?.segment?.chapter)
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
        }

    @Test
    fun `GIVEN a partial chapter in the day WHEN its last verse ends THEN stops before the next verse`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startDayReading(
                day = createPartialDay(
                    startVerse = 1,
                    endVerse = 2,
                ),
                chapter = genesisOne,
            )

            // When
            audioPlayer.position = 10.seconds
            advanceTimeBy(250.milliseconds)

            // Then
            assertEquals(ListeningStatusModel.FINISHED, session?.status)
            assertEquals(genesisOne, session?.finishedChapter)
        }

    @Test
    fun `GIVEN the reading of today WHEN the reader opens a chapter outside it THEN leaves the day`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startDayReading(
                day = createDay(genesisOne, genesisThree),
                chapter = genesisOne,
            )

            // When
            controller.followReader(genesisTwo)

            // Then
            assertEquals(ListeningModeModel.CHAPTER, session?.mode)
            assertNull(session?.day)
        }

    @Test
    fun `GIVEN a finished chapter offer WHEN dismissing it THEN clears it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isAutoNextEnabled = false)
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        audioPlayer.finish()

        // When
        controller.dismissFinishOffer()

        // Then
        assertNull(session?.finishedChapter)
    }

    @Test
    fun `GIVEN a playback the player cannot decode WHEN it fails THEN shows the audio as unavailable`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            audioPlayer.emit(
                ChapterAudioPlayerEvent.Failed(
                    playbackId = audioPlayer.plays.last().playbackId,
                    isFileUnreadable = true,
                ),
            )

            // Then
            assertEquals(ListeningStatusModel.AUDIO_UNAVAILABLE, session?.status)
            assertEquals(listOf(genesisOneAudio.audioPath), audioRepository.evicted)
        }

    @Test
    fun `GIVEN a paused chapter WHEN skipping to a locked one and playing THEN asks to unlock it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(lockedChapters = setOf(genesisThree))
            controller.startChapter(
                chapter = genesisTwo,
                shouldForceCanonOrder = false,
            )
            controller.togglePlayPause()
            controller.nextChapter()

            // When
            controller.togglePlayPause()

            // Then
            assertEquals(ListeningStatusModel.NEXT_LOCKED, session?.status)
            assertEquals(genesisThree, session?.lockedSegment?.chapter)
            assertFalse(genesisThree in audioRepository.requests)
        }

    @Test
    fun `GIVEN a playback that failed mid chapter WHEN retrying THEN resumes where it was`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        audioPlayer.position = 8.seconds
        audioPlayer.emit(
            ChapterAudioPlayerEvent.Failed(
                playbackId = audioPlayer.plays.last().playbackId,
                isFileUnreadable = false,
            ),
        )

        // When
        controller.togglePlayPause()

        // Then
        assertEquals(8.seconds, audioPlayer.plays.last().position)
    }

    @Test
    fun `GIVEN a playback that failed mid chapter WHEN closed and started again THEN starts from the beginning`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            val firstStart = audioPlayer.plays.last().position
            audioPlayer.position = 8.seconds
            audioPlayer.emit(
                ChapterAudioPlayerEvent.Failed(
                    playbackId = audioPlayer.plays.last().playbackId,
                    isFileUnreadable = false,
                ),
            )
            controller.stop()

            // When
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // Then
            assertEquals(firstStart, audioPlayer.plays.last().position)
        }

    @Test
    fun `GIVEN the end of chapter sleep timer WHEN a chapter plays THEN prefetches nothing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.setSleepTimer(ListeningSleepTimerOption.END_OF_CHAPTER)

            // When
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // Then
            assertEquals(listOf(genesisOne), audioRepository.requests)
        }

    @Test
    fun `GIVEN a playback failure WHEN it fails THEN tracks the audio as unavailable`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        audioPlayer.emit(
            ChapterAudioPlayerEvent.Failed(
                playbackId = audioPlayer.plays.last().playbackId,
                isFileUnreadable = false,
            ),
        )

        // Then
        assertEquals(
            "playback_failed",
            trackedEvents.last { (name, _) -> name == "chapter_listening_audio_unavailable" }.second["reason"],
        )
    }

    @Test
    fun `GIVEN a playback failing for another reason WHEN it fails THEN keeps the cached file`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            audioPlayer.emit(
                ChapterAudioPlayerEvent.Failed(
                    playbackId = audioPlayer.plays.last().playbackId,
                    isFileUnreadable = false,
                ),
            )

            // Then
            assertEquals(ListeningStatusModel.AUDIO_UNAVAILABLE, session?.status)
            assertTrue(audioRepository.evicted.isEmpty())
        }

    @Test
    fun `GIVEN a paused chapter WHEN skipping to the next chapter THEN moves without loading it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisTwo,
                shouldForceCanonOrder = false,
            )
            controller.togglePlayPause()

            // When
            controller.nextChapter()

            // Then
            assertEquals(genesisThree, session?.segment?.chapter)
            assertEquals(ListeningStatusModel.PAUSED, session?.status)
            assertFalse(mediaSession.updates.last().isAudioLoaded)
        }

    @Test
    fun `GIVEN an expired session WHEN the person signs in again THEN loads and plays the chapter`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(failure = ChapterAudioFailureModel.NOT_SIGNED_IN)
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            authenticatedUserId.value = null
            audioRepository.failure = null

            // When
            authenticatedUserId.value = "user-1"

            // Then
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
            assertEquals(1, audioPlayer.plays.size)
        }

    @Test
    fun `GIVEN an expired session WHEN an account without access signs in THEN asks to unlock the chapter`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                failure = ChapterAudioFailureModel.NOT_SIGNED_IN,
                lockedChapters = setOf(genesisOne),
            )
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            authenticatedUserId.value = null
            audioRepository.failure = null

            // When
            authenticatedUserId.value = "user-2"

            // Then
            assertEquals(ListeningStatusModel.NEXT_LOCKED, session?.status)
            assertEquals(genesisOne, session?.lockedSegment?.chapter)
            assertTrue(audioPlayer.plays.isEmpty())
        }

    @Test
    fun `GIVEN a chapter that needs a connection WHEN someone signs in THEN waits for a retry`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(failure = ChapterAudioFailureModel.NO_CONNECTION)
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            authenticatedUserId.value = null

            // When
            authenticatedUserId.value = "user-2"

            // Then
            assertEquals(ListeningStatusModel.NEEDS_CONNECTION, session?.status)
            assertEquals(listOf(genesisOne), audioRepository.requests)
        }

    @Test
    fun `GIVEN a chapter playing WHEN the session leaves it THEN checks it for a newer revision`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isAutoNextEnabled = false)
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            val refreshedWhilePlaying = audioRepository.refreshed.toList()

            // When
            controller.nextChapter()

            // Then
            assertTrue(refreshedWhilePlaying.isEmpty())
            assertEquals(listOf(genesisOne), audioRepository.refreshed)
        }

    @Test
    fun `GIVEN a manifest without the segment verses WHEN starting THEN shows the audio as unavailable`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            controller.startDayReading(
                day = createPartialDay(
                    startVerse = 40,
                    endVerse = 45,
                ),
                chapter = genesisOne,
            )

            // Then
            assertEquals(ListeningStatusModel.AUDIO_UNAVAILABLE, session?.status)
            assertEquals(listOf(genesisOneAudio.audioPath), audioRepository.evicted)
            assertEquals(
                "no_verses",
                trackedEvents.last { (name, _) -> name == "chapter_listening_audio_unavailable" }.second["reason"],
            )
        }

    @Test
    fun `GIVEN the access check fails WHEN prefetching the next chapter THEN keeps playing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isAccessCheckFailing = true)

            // When
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // Then
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
        }

    @Test
    fun `GIVEN the audio focus is refused WHEN starting THEN stays paused`() = runTest(testDispatcher) {
        // Given
        prepareScenario(canActivate = false)

        // When
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
        assertTrue(audioPlayer.plays.isEmpty())
    }

    @Test
    fun `GIVEN a chapter playing WHEN the state changes THEN tells the system what is playing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // Then
            val nowPlaying = mediaSession.updates.last()
            assertEquals("Genesis 1", nowPlaying.chapterTitle)
            assertEquals("ACF", nowPlaying.subtitle)
            assertEquals(14.seconds, nowPlaying.time.total)
            assertEquals(1, nowPlaying.verseNumber)
            assertTrue(nowPlaying.isPlaying)
        }

    private fun createAudioVerse(
        number: Int,
        headingStart: Duration?,
        start: Duration,
        end: Duration,
        headingWords: List<ChapterAudioWordModel> = emptyList(),
        words: List<ChapterAudioWordModel> = emptyList(),
    ): ChapterAudioVerseModel = ChapterAudioVerseModel(
        number = number,
        headingStart = headingStart,
        headingWords = headingWords,
        start = start,
        end = end,
        words = words,
    )

    private fun createAudioWord(
        offset: Int,
        start: Duration,
        end: Duration,
    ): ChapterAudioWordModel = ChapterAudioWordModel(
        offset = offset,
        start = start,
        end = end,
    )

    private fun spokenTarget(): ListeningWordTargetModel? {
        val current = session ?: return null
        val wordIndex = current.wordIndex ?: return null
        return current.verses[current.verseIndex].words[wordIndex].target
    }

    private fun createDay(vararg chapters: ChapterLocationModel): ListeningDayModel = ListeningDayModel(
        location = PlanDayLocationModel(
            weekNumber = 1,
            dayNumber = 1,
            readingPlanType = ReadingPlanType.BOOKS,
        ),
        segments = chapters.map { chapter ->
            ListeningSegmentModel(
                chapter = chapter,
                startVerse = null,
                endVerse = null,
            )
        },
    )

    private fun createPartialDay(
        startVerse: Int,
        endVerse: Int,
    ): ListeningDayModel = ListeningDayModel(
        location = PlanDayLocationModel(
            weekNumber = 1,
            dayNumber = 1,
            readingPlanType = ReadingPlanType.BOOKS,
        ),
        segments = listOf(
            ListeningSegmentModel(
                chapter = genesisOne,
                startVerse = startVerse,
                endVerse = endVerse,
            ),
        ),
    )

    private fun TestScope.prepareScenario(
        failure: ChapterAudioFailureModel? = null,
        isAutoNextEnabled: Boolean = true,
        isWordHighlightEnabled: Boolean = true,
        lockedChapters: Set<ChapterLocationModel> = emptySet(),
        accessGate: CompletableDeferred<Unit>? = null,
        canActivate: Boolean = true,
        isAccessCheckFailing: Boolean = false,
    ) {
        audioPlayer = FakeChapterAudioPlayer()
        audioRepository = FakeChapterAudioRepository(audioByChapter)
        audioRepository.failure = failure
        authenticatedUserId = MutableStateFlow("user-1")
        mediaSession = FakeListeningMediaSession(canActivate)
        settingsRepository = FakeSettingsRepository(
            ChapterListeningSettingsModel(
                speed = 1f,
                isAutoNextEnabled = isAutoNextEnabled,
                isWordHighlightEnabled = isWordHighlightEnabled,
            ),
        )
        trackedEvents = mutableListOf()
        emittedEvents = mutableListOf()
        val order = listOf(genesisOne, genesisTwo, genesisThree)
        controller = createChapterListeningController(
            scope = backgroundScope,
            audioPlayer = audioPlayer,
            mediaSession = mediaSession,
            settingsRepository = settingsRepository,
            chapterAudioRepository = audioRepository,
            observeAuthenticatedUserId = { authenticatedUserId },
            getChapterVerseContents = { chapter ->
                val location = ChapterLocationModel(chapter.bookId, chapter.chapterNumber)
                val headings = headingsByChapter[location].orEmpty()
                versesByChapter[location].orEmpty().mapValues { (number, text) ->
                    ChapterVerseContentModel(
                        text = text,
                        heading = headings[number],
                    )
                }
            },
            getSelectedVersionIdFlow = { flowOf("ACF") },
            getChapterListeningAccess = { chapter ->
                accessGate?.await()
                if (isAccessCheckFailing) error("Remote config unavailable")
                if (chapter in lockedChapters) {
                    ChapterListeningAccessModel.UnlockAvailable(
                        rewardedRemainingToday = 1,
                        isFreeUnlockAvailable = true,
                        allowance = ListeningAllowanceModel(
                            hasRewardedUnlocks = true,
                            hasFreeUnlocks = true,
                        ),
                    )
                } else {
                    ChapterListeningAccessModel.Open
                }
            },
            getAdjacentListeningChapter = { chapter, direction, _ ->
                val step = if (direction == ChapterDirectionModel.NEXT) 1 else -1
                order.getOrNull(order.indexOf(chapter) + step)
            },
            getListeningChapterTitle = { chapter -> "Genesis ${chapter.chapterNumber}" },
            getListeningTime = GetListeningTimeUseCase(),
            getListeningWords = GetListeningWordsUseCase(),
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        backgroundScope.launch { controller.events.collect { event -> emittedEvents += event } }
    }
}

private class FakeSettingsRepository(
    initial: ChapterListeningSettingsModel,
) : ChapterListeningSettingsRepository {
    val settings = MutableStateFlow(initial)

    override fun observe(): Flow<ChapterListeningSettingsModel> = settings

    override suspend fun setSpeed(speed: Float) {
        settings.update { it.copy(speed = speed) }
    }

    override suspend fun setAutoNextEnabled(isEnabled: Boolean) {
        settings.update { it.copy(isAutoNextEnabled = isEnabled) }
    }

    override suspend fun setWordHighlightEnabled(isEnabled: Boolean) {
        settings.update { it.copy(isWordHighlightEnabled = isEnabled) }
    }
}
