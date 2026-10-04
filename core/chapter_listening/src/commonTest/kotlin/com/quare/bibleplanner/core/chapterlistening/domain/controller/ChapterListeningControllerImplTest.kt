package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVersionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVoiceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechEngineEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechUtteranceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechVoicesModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningSettingsRepository
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.domain.service.SpeechEngine
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.EstimateListeningTimeUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
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
    private val voice = ListeningVoiceModel(
        id = "voice-1",
        name = "Luciana",
        languageTag = "pt-BR",
        isEnhanced = true,
    )
    private val otherVoice = ListeningVoiceModel(
        id = "voice-2",
        name = "Felipe",
        languageTag = "pt-BR",
        isEnhanced = false,
    )
    private val chapterGap = 1.5.seconds

    private lateinit var controller: ChapterListeningControllerImpl
    private lateinit var speechEngine: FakeSpeechEngine
    private lateinit var mediaSession: FakeListeningMediaSession
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var emittedEvents: MutableList<ChapterListeningEventModel>

    private val session: ListeningSessionModel?
        get() = controller.state.value.session

    @Test
    fun `GIVEN a chapter WHEN starting it THEN reads every verse from the first one`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // Then
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
        assertEquals(3, session?.verses?.size)
        assertEquals(
            expected = versesByChapter.getValue(genesisOne).values.toList(),
            actual = speechEngine.lastUtterances.map(SpeechUtteranceModel::text),
        )
        assertEquals(voice.id, speechEngine.speakCalls.last().voiceId)
        assertEquals("chapter_listening_started", trackedEvents.last().first)
    }

    @Test
    fun `GIVEN no voice for the language WHEN starting THEN waits for one without speaking`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(voices = SpeechVoicesModel.Unavailable)

            // When
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // Then
            assertEquals(ListeningStatusModel.VOICE_UNAVAILABLE, session?.status)
            assertTrue(speechEngine.speakCalls.isEmpty())
            assertEquals("chapter_listening_voice_unavailable", trackedEvents.last().first)
        }

    @Test
    fun `GIVEN a voice installed later WHEN resuming THEN starts reading`() = runTest(testDispatcher) {
        // Given
        prepareScenario(voices = SpeechVoicesModel.Unavailable)
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        speechEngine.voices = SpeechVoicesModel.Available(
            voices = listOf(voice),
            shouldSuggestEnhancedVoice = false,
        )

        // When
        controller.togglePlayPause()

        // Then
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
    }

    @Test
    fun `GIVEN a chapter being read WHEN the engine reaches a word THEN shows the verse and its progress`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            val secondVerseId = speechEngine.lastUtterances[1].id

            // When
            speechEngine.emit(SpeechEngineEvent.Started(secondVerseId))
            speechEngine.emit(
                SpeechEngineEvent.RangeStarted(
                    utteranceId = secondVerseId,
                    charIndex = 21,
                ),
            )

            // Then
            assertEquals(1, session?.verseIndex)
            assertEquals(21f / versesByChapter.getValue(genesisOne).getValue(2).length, session?.verseProgress)
        }

    @Test
    fun `GIVEN a stopped utterance WHEN its late callback arrives THEN ignores it`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        val staleId = speechEngine.lastUtterances[2].id
        controller.togglePlayPause()

        // When
        speechEngine.emit(SpeechEngineEvent.Started(staleId))

        // Then
        assertEquals(0, session?.verseIndex)
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
    }

    @Test
    fun `GIVEN the last verse and auto next on WHEN it finishes THEN moves on to the next chapter after a pause`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            speechEngine.emit(SpeechEngineEvent.Finished(speechEngine.lastUtterances.last().id))

            // Then
            assertEquals(genesisTwo, session?.segment?.chapter)
            assertEquals(ChapterChangeCause.PLAYER, session?.chapterChangeCause)
            assertEquals(genesisOne, session?.finishedChapter)
            assertEquals(chapterGap, speechEngine.lastUtterances.first().leadingSilence)
            assertTrue(trackedEvents.any { (name, _) -> name == "chapter_listening_completed" })
        }

    @Test
    fun `GIVEN auto next off WHEN the last verse finishes THEN stops at the end of the chapter`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isAutoNextEnabled = false)
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            speechEngine.emit(SpeechEngineEvent.Finished(speechEngine.lastUtterances.last().id))

            // Then
            assertEquals(ListeningStatusModel.FINISHED, session?.status)
            assertEquals(genesisOne, session?.finishedChapter)
            assertEquals(1f, session?.verseProgress)
        }

    @Test
    fun `GIVEN a finished chapter WHEN playing again THEN reads it from the start`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isAutoNextEnabled = false)
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        speechEngine.emit(SpeechEngineEvent.Finished(speechEngine.lastUtterances.last().id))

        // When
        controller.togglePlayPause()

        // Then
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
        assertEquals(3, speechEngine.lastUtterances.size)
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
        speechEngine.emit(SpeechEngineEvent.Finished(speechEngine.lastUtterances.last().id))

        // Then
        assertEquals(ListeningStatusModel.NEXT_LOCKED, session?.status)
        assertEquals(genesisTwo, session?.lockedSegment?.chapter)
        assertEquals(genesisOne, session?.segment?.chapter)
        assertEquals("chapter_listening_locked", trackedEvents.last().first)
    }

    @Test
    fun `GIVEN a locked chapter WHEN it is unlocked THEN reads it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(lockedChapters = setOf(genesisTwo))
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        speechEngine.emit(SpeechEngineEvent.Finished(speechEngine.lastUtterances.last().id))

        // When
        controller.continueLockedChapter()

        // Then
        assertEquals(genesisTwo, session?.segment?.chapter)
        assertEquals(ListeningStatusModel.PLAYING, session?.status)
        assertNull(session?.lockedSegment)
    }

    @Test
    fun `GIVEN a paused verse WHEN resuming THEN continues from the start of the word it stopped in`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            val firstVerseId = speechEngine.lastUtterances.first().id
            speechEngine.emit(
                SpeechEngineEvent.RangeStarted(
                    utteranceId = firstVerseId,
                    charIndex = 6,
                ),
            )
            controller.togglePlayPause()

            // When
            controller.togglePlayPause()

            // Then
            assertEquals(
                "the beginning God created the heaven and the earth.",
                speechEngine.lastUtterances.first().text,
            )
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
        }

    @Test
    fun `GIVEN a chapter being read WHEN a call interrupts it THEN shows it as interrupted`() =
        runTest(testDispatcher) {
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
    fun `GIVEN an interrupted chapter WHEN the interruption ends THEN reads on`() = runTest(testDispatcher) {
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
    fun `GIVEN a chapter being read WHEN the reader opens a locked chapter THEN keeps reading the current one`() =
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
    fun `GIVEN a locked next chapter WHEN the notification skips to it THEN keeps reading the current one`() =
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
    fun `GIVEN a chapter being read WHEN asking for its neighbours THEN follows the reading order`() =
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
    fun `GIVEN a chapter being read WHEN the headphones are unplugged THEN pauses`() = runTest(testDispatcher) {
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
    fun `GIVEN a chapter being read WHEN the lock screen skips to a verse THEN reads from it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            mediaSession.commands.emit(ListeningRemoteCommand.SkipToVerse(2))

            // Then
            assertEquals(2, session?.verseIndex)
            assertEquals(
                listOf(versesByChapter.getValue(genesisOne).getValue(3)),
                speechEngine.lastUtterances.map { it.text },
            )
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
    fun `GIVEN the previous chapter command WHEN it arrives THEN reads the previous chapter`() =
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
            val secondVerseId = speechEngine.lastUtterances[1].id
            speechEngine.emit(
                SpeechEngineEvent.RangeStarted(
                    utteranceId = secondVerseId,
                    charIndex = 21,
                ),
            )

            // When
            controller.previousVerse()

            // Then
            assertEquals(1, session?.verseIndex)
            assertEquals(versesByChapter.getValue(genesisOne).getValue(2), speechEngine.lastUtterances.first().text)
        }

    @Test
    fun `GIVEN a paused chapter WHEN skipping verses THEN moves without speaking`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )
        controller.togglePlayPause()
        val speakCount = speechEngine.speakCalls.size

        // When
        controller.skipToVerse(2)

        // Then
        assertEquals(2, session?.verseIndex)
        assertEquals(speakCount, speechEngine.speakCalls.size)
    }

    @Test
    fun `GIVEN a chapter being read WHEN changing the speed THEN reads on at the new speed and saves it`() =
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
            assertEquals(1.5f, speechEngine.speakCalls.last().speed)
            assertEquals(1.5f, settingsRepository.settings.value.speed)
        }

    @Test
    fun `GIVEN a chapter being read WHEN choosing another voice THEN reads on with it and saves it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )

            // When
            controller.selectVoice(otherVoice.id)

            // Then
            assertEquals(otherVoice.id, speechEngine.speakCalls.last().voiceId)
            assertEquals(otherVoice.id, controller.state.value.selectedVoiceId)
            assertEquals(otherVoice.id, settingsRepository.settings.value.voiceId)
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
    fun `GIVEN a chapter being read WHEN previewing a voice THEN pauses and resumes once the sample ends`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            controller.startChapter(
                chapter = genesisOne,
                shouldForceCanonOrder = false,
            )
            controller.previewVoice(
                voiceId = otherVoice.id,
                sampleText = "Sample",
            )
            val previewId = speechEngine.lastUtterances.single().id
            val previewingVoiceId = controller.state.value.previewingVoiceId

            // When
            speechEngine.emit(SpeechEngineEvent.Finished(previewId))

            // Then
            assertEquals(otherVoice.id, previewingVoiceId)
            assertNull(controller.state.value.previewingVoiceId)
            assertEquals(ListeningStatusModel.PLAYING, session?.status)
        }

    @Test
    fun `GIVEN a sleep timer WHEN its time runs out while reading THEN pauses and says good night`() =
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
            speechEngine.emit(SpeechEngineEvent.Finished(speechEngine.lastUtterances.last().id))

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
    fun `GIVEN a chapter being read WHEN the reader opens another chapter THEN reads that one`() =
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
            val day = createDay(genesisOne, genesisThree)
            controller.startDayReading(
                day = day,
                chapter = genesisOne,
            )

            // When
            speechEngine.emit(SpeechEngineEvent.Finished(speechEngine.lastUtterances.last().id))

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
        speechEngine.emit(SpeechEngineEvent.Finished(speechEngine.lastUtterances.last().id))

        // Then
        assertEquals(ListeningStatusModel.FINISHED, session?.status)
        assertEquals(genesisThree, session?.finishedChapter)
    }

    @Test
    fun `GIVEN a partial chapter in the day WHEN reading it THEN reads only its verses`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val day = ListeningDayModel(
            location = PlanDayLocationModel(
                weekNumber = 1,
                dayNumber = 1,
                readingPlanType = ReadingPlanType.BOOKS,
            ),
            segments = listOf(
                ListeningSegmentModel(
                    chapter = genesisOne,
                    startVerse = 2,
                    endVerse = 3,
                ),
            ),
        )

        // When
        controller.startDayReading(
            day = day,
            chapter = genesisOne,
        )

        // Then
        assertEquals(listOf(2, 3), session?.verses?.map { it.number })
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
        speechEngine.emit(SpeechEngineEvent.Finished(speechEngine.lastUtterances.last().id))

        // When
        controller.dismissFinishOffer()

        // Then
        assertNull(session?.finishedChapter)
    }

    @Test
    fun `GIVEN a failing utterance WHEN the engine reports it THEN pauses`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        controller.startChapter(
            chapter = genesisOne,
            shouldForceCanonOrder = false,
        )

        // When
        speechEngine.emit(SpeechEngineEvent.Failed(speechEngine.lastUtterances.first().id))

        // Then
        assertEquals(ListeningStatusModel.PAUSED, session?.status)
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
        assertTrue(speechEngine.speakCalls.isEmpty())
    }

    @Test
    fun `GIVEN a chapter being read WHEN the state changes THEN tells the system what is playing`() =
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
            assertEquals(3, nowPlaying.verses.size)
            assertTrue(nowPlaying.isPlaying)
        }

    @Test
    fun `GIVEN the voice settings WHEN opening them THEN asks the engine`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        controller.openVoiceSettings()

        // Then
        assertEquals(1, speechEngine.voiceSettingsOpenCount)
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

    private fun TestScope.prepareScenario(
        voices: SpeechVoicesModel = SpeechVoicesModel.Available(
            voices = listOf(voice, otherVoice),
            shouldSuggestEnhancedVoice = false,
        ),
        isAutoNextEnabled: Boolean = true,
        lockedChapters: Set<ChapterLocationModel> = emptySet(),
        canActivate: Boolean = true,
    ) {
        speechEngine = FakeSpeechEngine(voices)
        mediaSession = FakeListeningMediaSession(canActivate)
        settingsRepository = FakeSettingsRepository(
            ChapterListeningSettingsModel(
                speed = 1f,
                voiceId = null,
                isAutoNextEnabled = isAutoNextEnabled,
            ),
        )
        trackedEvents = mutableListOf()
        emittedEvents = mutableListOf()
        val order = listOf(genesisOne, genesisTwo, genesisThree)
        controller = ChapterListeningControllerImpl(
            scope = backgroundScope,
            speechEngine = speechEngine,
            mediaSession = mediaSession,
            settingsRepository = settingsRepository,
            getChapterVerseTexts = { chapter ->
                versesByChapter[ChapterLocationModel(chapter.bookId, chapter.chapterNumber)].orEmpty()
            },
            getListeningVersion = {
                ListeningVersionModel(
                    id = "ACF",
                    languageTag = "pt-BR",
                )
            },
            getChapterListeningAccess = { chapter ->
                if (chapter in lockedChapters) {
                    ChapterListeningAccessModel.UnlockAvailable(1)
                } else {
                    ChapterListeningAccessModel.Open
                }
            },
            getAdjacentListeningChapter = { chapter, direction, _ ->
                val step = if (direction == ChapterDirectionModel.NEXT) 1 else -1
                order.getOrNull(order.indexOf(chapter) + step)
            },
            getListeningChapterTitle = { chapter -> "Genesis ${chapter.chapterNumber}" },
            estimateListeningTime = EstimateListeningTimeUseCase(),
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        backgroundScope.launch { controller.events.collect { event -> emittedEvents += event } }
    }
}

private class FakeSpeechEngine(
    var voices: SpeechVoicesModel,
) : SpeechEngine {
    override val isSupported: Boolean = true
    private val eventFlow = MutableSharedFlow<SpeechEngineEvent>(extraBufferCapacity = 16)
    override val events: Flow<SpeechEngineEvent> = eventFlow
    val speakCalls = mutableListOf<SpeakCall>()
    var voiceSettingsOpenCount = 0

    val lastUtterances: List<SpeechUtteranceModel>
        get() = speakCalls.last().utterances

    fun emit(event: SpeechEngineEvent) {
        eventFlow.tryEmit(event)
    }

    override suspend fun loadVoices(languageTag: String): SpeechVoicesModel = voices

    override fun speak(
        utterances: List<SpeechUtteranceModel>,
        voiceId: String?,
        languageTag: String,
        speed: Float,
    ) {
        speakCalls += SpeakCall(
            utterances = utterances,
            voiceId = voiceId,
            speed = speed,
        )
    }

    override fun stop() {
    }

    override fun openVoiceSettings() {
        voiceSettingsOpenCount++
    }
}

private class SpeakCall(
    val utterances: List<SpeechUtteranceModel>,
    val voiceId: String?,
    val speed: Float,
)

private class FakeListeningMediaSession(
    private val canActivate: Boolean,
) : ListeningMediaSession {
    override val commands = MutableSharedFlow<ListeningRemoteCommand>(extraBufferCapacity = 16)
    override val interruptions = MutableSharedFlow<AudioInterruptionModel>(extraBufferCapacity = 16)
    val updates = mutableListOf<NowPlayingModel>()
    var deactivationCount = 0

    override fun requestActivation(): Boolean = canActivate

    override fun update(nowPlaying: NowPlayingModel) {
        updates += nowPlaying
    }

    override fun deactivate() {
        deactivationCount++
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

    override suspend fun setVoiceId(voiceId: String) {
        settings.update { it.copy(voiceId = voiceId) }
    }

    override suspend fun setAutoNextEnabled(isEnabled: Boolean) {
        settings.update { it.copy(isAutoNextEnabled = isEnabled) }
    }
}
