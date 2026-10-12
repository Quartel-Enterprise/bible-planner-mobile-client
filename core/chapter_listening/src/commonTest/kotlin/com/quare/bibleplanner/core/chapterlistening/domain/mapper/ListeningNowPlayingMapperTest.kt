package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningStateModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningTimeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningTimeUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ListeningNowPlayingMapperTest {
    private val genesisOne = createChapter(1)
    private val genesisTwo = createChapter(2)

    // Verse 1 runs for 6 s and verse 2 for 4 s; the session is 35% through verse 2, at 7.4 s.
    private val session = createListeningSession(
        mode = ListeningModeModel.CHAPTER,
        day = null,
        segment = genesisOne.toWholeChapterSegment(),
        bibleVersionId = "acf",
        shouldForceCanonOrder = false,
    ).copy(
        verses = listOf(
            createVerse(
                number = 1,
                start = Duration.ZERO,
                end = 6.seconds,
            ),
            createVerse(
                number = 2,
                start = 6.seconds,
                end = 10.seconds,
            ),
        ),
        verseIndex = 1,
        verseProgress = 0.35f,
        status = ListeningStatusModel.PLAYING,
    )

    private lateinit var mapper: ListeningNowPlayingMapper
    private lateinit var titleRequests: MutableList<ChapterLocationModel>

    @BeforeTest
    fun setUp() {
        titleRequests = mutableListOf()
        mapper = ListeningNowPlayingMapper(
            getListeningChapterTitle = { chapter ->
                titleRequests += chapter
                "Genesis ${chapter.chapterNumber}"
            },
            getListeningTime = GetListeningTimeUseCase(),
        )
    }

    @Test
    fun `GIVEN no session WHEN mapping the state THEN there is nothing playing`() = runTest {
        // Given
        val state = createState(session = null)

        // When
        val nowPlaying = mapper.toNowPlayingOrNull(state)

        // Then
        assertNull(nowPlaying)
    }

    @Test
    fun `GIVEN a playing chapter WHEN mapping the state THEN shows its title verse and whole seconds`() = runTest {
        // Given
        val state = createState(session = session)

        // When
        val nowPlaying = mapper.toNowPlayingOrNull(state)

        // Then
        assertEquals(
            NowPlayingModel(
                chapterTitle = "Genesis 1",
                subtitle = "ACF",
                verseNumber = 2,
                time = ListeningTimeModel(
                    elapsed = 7.seconds,
                    total = 10.seconds,
                ),
                isAudioLoaded = true,
                isPlaying = true,
            ),
            nowPlaying,
        )
    }

    @Test
    fun `GIVEN a faster speed WHEN mapping the state THEN shortens the time`() = runTest {
        // Given
        val state = createState(
            session = session,
            speed = 2f,
        )

        // When
        val nowPlaying = mapper.toNowPlayingOrNull(state)

        // Then
        assertEquals(5.seconds, nowPlaying?.time?.total)
    }

    @Test
    fun `GIVEN a chapter still loading WHEN mapping the state THEN reports it as playing without audio`() = runTest {
        // Given
        val state = createState(
            session = session.copy(
                verses = emptyList(),
                verseIndex = 0,
                status = ListeningStatusModel.PREPARING,
            ),
        )

        // When
        val nowPlaying = mapper.toNowPlayingOrNull(state)

        // Then
        assertEquals(true, nowPlaying?.isPlaying)
        assertEquals(false, nowPlaying?.isAudioLoaded)
        assertNull(nowPlaying?.verseNumber)
    }

    @Test
    fun `GIVEN a paused chapter WHEN mapping the state THEN reports it as not playing`() = runTest {
        // Given
        val state = createState(session = session.copy(status = ListeningStatusModel.PAUSED))

        // When
        val nowPlaying = mapper.toNowPlayingOrNull(state)

        // Then
        assertEquals(false, nowPlaying?.isPlaying)
    }

    @Test
    fun `GIVEN a chapter already mapped WHEN mapping it again THEN looks its title up only once`() = runTest {
        // Given
        mapper.toNowPlayingOrNull(createState(session = session))

        // When
        mapper.toNowPlayingOrNull(createState(session = session.copy(verseIndex = 0)))

        // Then
        assertEquals(listOf(genesisOne), titleRequests)
    }

    @Test
    fun `GIVEN a chapter already mapped WHEN mapping another chapter THEN looks up the new title`() = runTest {
        // Given
        mapper.toNowPlayingOrNull(createState(session = session))
        val nextSession = session.copy(segment = genesisTwo.toWholeChapterSegment())

        // When
        val nowPlaying = mapper.toNowPlayingOrNull(createState(session = nextSession))

        // Then
        assertEquals("Genesis 2", nowPlaying?.chapterTitle)
        assertEquals(listOf(genesisOne, genesisTwo), titleRequests)
    }

    private fun createState(
        session: ListeningSessionModel?,
        speed: Float = 1f,
    ): ChapterListeningStateModel = ChapterListeningStateModel(
        session = session,
        settings = ChapterListeningSettingsModel(
            speed = speed,
            isAutoNextEnabled = true,
            isWordHighlightEnabled = true,
        ),
        sleepTimer = ListeningSleepTimerModel.Off,
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

    private fun createChapter(number: Int): ChapterLocationModel = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = number,
    )
}
