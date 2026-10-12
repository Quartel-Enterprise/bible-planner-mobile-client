package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.createListeningSession
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toWholeChapterSegment
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioFailureModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioResultModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration

internal class ChapterListeningAnalyticsTrackerTest {
    private val genesisOne = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = 1,
    )
    private val session = createListeningSession(
        mode = ListeningModeModel.DAY_READING,
        day = null,
        segment = genesisOne.toWholeChapterSegment(),
        bibleVersionId = "ACF",
        shouldForceCanonOrder = false,
    ).copy(chapterChangeCause = ChapterChangeCause.PLAYER)
    private val chapterParams = mapOf<String, Any>(
        "mode" to "day_reading",
        "book_id" to "gen",
        "chapter_number" to 1,
    )
    private val readyResult = ChapterAudioResultModel.Ready(
        ChapterAudioModel(
            audioPath = "/cache/GEN_1.mp3",
            titleWords = emptyList(),
            verses = emptyList(),
            duration = Duration.ZERO,
        ),
    )

    private lateinit var tracker: ChapterListeningAnalyticsTracker
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        trackedEvents = mutableListOf()
        tracker = ChapterListeningAnalyticsTracker { name, params -> trackedEvents += name to params }
    }

    @Test
    fun `GIVEN a failed audio WHEN tracking the opened chapter THEN tracks it as unavailable with the failure`() {
        // Given
        val result = ChapterAudioResultModel.Failed(ChapterAudioFailureModel.NO_CONNECTION)

        // When
        tracker.trackOpened(
            session = session,
            result = result,
            status = ListeningStatusModel.NEEDS_CONNECTION,
        )

        // Then
        assertEquals(
            listOf("chapter_listening_audio_unavailable" to chapterParams + ("reason" to "no_connection")),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN an audio without the segment verses WHEN tracking the opened chapter THEN tracks it as unavailable`() {
        // Given
        val status = ListeningStatusModel.AUDIO_UNAVAILABLE

        // When
        tracker.trackOpened(
            session = session,
            result = readyResult,
            status = status,
        )

        // Then
        assertEquals(
            listOf("chapter_listening_audio_unavailable" to chapterParams + ("reason" to "no_verses")),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN a ready audio WHEN tracking the opened chapter THEN tracks nothing`() {
        // Given
        val status = ListeningStatusModel.PAUSED

        // When
        tracker.trackOpened(
            session = session,
            result = readyResult,
            status = status,
        )

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a playback that failed WHEN tracking it THEN tracks the audio as unavailable`() {
        // Given
        val failedSession = session

        // When
        tracker.trackPlaybackFailed(failedSession)

        // Then
        assertEquals(
            listOf("chapter_listening_audio_unavailable" to chapterParams + ("reason" to "playback_failed")),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN a chapter that starts WHEN tracking it THEN tracks the start with its cause`() {
        // Given
        val startedSession = session

        // When
        tracker.trackStarted(startedSession)

        // Then
        assertEquals(
            listOf("chapter_listening_started" to chapterParams + ("cause" to "player")),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN a chapter heard to the end WHEN tracking it THEN tracks it as completed`() {
        // Given
        val completedSession = session

        // When
        tracker.trackCompleted(completedSession)

        // Then
        assertEquals(listOf("chapter_listening_completed" to chapterParams), trackedEvents)
    }

    @Test
    fun `GIVEN a locked segment WHEN tracking it THEN tracks its chapter as locked`() {
        // Given
        val segment = ListeningSegmentModel(
            chapter = ChapterLocationModel(
                bookId = BookId.EXO,
                chapterNumber = 3,
            ),
            startVerse = 1,
            endVerse = 5,
        )

        // When
        tracker.trackLocked(segment)

        // Then
        assertEquals(
            listOf(
                "chapter_listening_locked" to mapOf<String, Any>(
                    "book_id" to "exo",
                    "chapter_number" to 3,
                ),
            ),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN a chapter interrupted WHEN tracking it THEN tracks the interruption`() {
        // Given
        val interruptedSession = session

        // When
        tracker.trackInterrupted(interruptedSession)

        // Then
        assertEquals(listOf("chapter_listening_interrupted" to chapterParams), trackedEvents)
    }

    @Test
    fun `GIVEN a sleep timer that ended WHEN tracking it THEN tracks its option`() {
        // Given
        val option = "countdown"

        // When
        tracker.trackSleepTimerEnded(option)

        // Then
        assertEquals(
            listOf("chapter_listening_sleep_timer_ended" to mapOf<String, Any>("option" to option)),
            trackedEvents,
        )
    }
}
