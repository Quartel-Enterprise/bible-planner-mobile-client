package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ListeningSessionMapperTest {
    private val genesisOne = createChapter(1)
    private val genesisTwo = createChapter(2)
    private val genesisThree = createChapter(3)
    private val partialSegment = ListeningSegmentModel(
        chapter = genesisTwo,
        startVerse = 4,
        endVerse = 9,
    )
    private val day = ListeningDayModel(
        location = PlanDayLocationModel(
            weekNumber = 2,
            dayNumber = 3,
            readingPlanType = ReadingPlanType.BOOKS,
        ),
        segments = listOf(genesisOne.toWholeChapterSegment(), partialSegment),
    )
    private val verse = ListeningVerseModel(
        number = 1,
        text = "In the beginning",
        start = Duration.ZERO,
        end = 4.seconds,
        words = emptyList(),
    )

    // A day reading playing its first chapter, half through its only verse, with a locked chapter waiting.
    private val playingSession = ListeningSessionModel(
        mode = ListeningModeModel.DAY_READING,
        day = day,
        segment = genesisOne.toWholeChapterSegment(),
        bibleVersionId = "ACF",
        shouldForceCanonOrder = true,
        verses = listOf(verse),
        verseIndex = 0,
        verseProgress = 0.5f,
        wordIndex = 2,
        status = ListeningStatusModel.PLAYING,
        chapterChangeCause = ChapterChangeCause.PLAYER,
        finishedChapter = genesisThree,
        lockedSegment = partialSegment,
    )

    @Test
    fun `GIVEN a segment to listen to WHEN creating the session THEN it prepares the segment from its start`() {
        // Given
        val segment = partialSegment

        // When
        val session = createListeningSession(
            mode = ListeningModeModel.DAY_READING,
            day = day,
            segment = segment,
            bibleVersionId = "NVI",
            shouldForceCanonOrder = true,
        )

        // Then
        assertEquals(
            ListeningSessionModel(
                mode = ListeningModeModel.DAY_READING,
                day = day,
                segment = segment,
                bibleVersionId = "NVI",
                shouldForceCanonOrder = true,
                verses = emptyList(),
                verseIndex = 0,
                verseProgress = 0f,
                wordIndex = null,
                status = ListeningStatusModel.PREPARING,
                chapterChangeCause = ChapterChangeCause.START,
                finishedChapter = null,
                lockedSegment = null,
            ),
            session,
        )
    }

    @Test
    fun `GIVEN a day reading WHEN the reader opens one of its chapters THEN keeps the day and plays its segment`() {
        // Given
        val chapter = genesisTwo

        // When
        val readerSession = playingSession.toReaderSession(chapter)

        // Then
        assertEquals(
            playingSession.copy(
                segment = partialSegment,
                chapterChangeCause = ChapterChangeCause.READER,
            ),
            readerSession,
        )
    }

    @Test
    fun `GIVEN a day reading WHEN the reader opens a chapter outside it THEN listens to that whole chapter alone`() {
        // Given
        val chapter = genesisThree

        // When
        val readerSession = playingSession.toReaderSession(chapter)

        // Then
        assertEquals(
            playingSession.copy(
                mode = ListeningModeModel.CHAPTER,
                day = null,
                segment = genesisThree.toWholeChapterSegment(),
                chapterChangeCause = ChapterChangeCause.READER,
            ),
            readerSession,
        )
    }

    @Test
    fun `GIVEN a single chapter WHEN the reader opens another chapter THEN listens to that whole chapter`() {
        // Given
        val chapterSession = playingSession.copy(
            mode = ListeningModeModel.CHAPTER,
            day = null,
        )

        // When
        val readerSession = chapterSession.toReaderSession(genesisTwo)

        // Then
        assertEquals(ListeningModeModel.CHAPTER, readerSession.mode)
        assertEquals(genesisTwo.toWholeChapterSegment(), readerSession.segment)
        assertEquals(ChapterChangeCause.READER, readerSession.chapterChangeCause)
    }

    @Test
    fun `GIVEN a loaded session WHEN unloading it THEN clears its audio and lock and shows the status`() {
        // Given
        val status = ListeningStatusModel.PREPARING

        // When
        val unloaded = playingSession.toUnloaded(status)

        // Then
        assertEquals(
            playingSession.copy(
                verses = emptyList(),
                verseIndex = 0,
                verseProgress = 0f,
                wordIndex = null,
                status = status,
                lockedSegment = null,
            ),
            unloaded,
        )
    }

    @Test
    fun `GIVEN a playing session WHEN its playback fails THEN drops its verses and shows the audio as unavailable`() {
        // Given
        val session = playingSession

        // When
        val failed = session.toPlaybackFailed()

        // Then
        assertEquals(
            playingSession.copy(
                status = ListeningStatusModel.AUDIO_UNAVAILABLE,
                verses = emptyList(),
                verseIndex = 0,
                verseProgress = 0f,
                wordIndex = null,
            ),
            failed,
        )
    }

    private fun createChapter(number: Int): ChapterLocationModel = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = number,
    )
}
