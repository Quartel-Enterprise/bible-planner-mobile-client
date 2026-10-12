package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.books.domain.model.ChapterVerseContentModel
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.createListeningSession
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toUnloaded
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioFailureModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.FindAdjacentListeningSegmentUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningVersesUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningWordsUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class ListeningSegmentLoaderTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val genesisOne = createChapter(1)
    private val genesisTwo = createChapter(2)
    private val wholeChapter = createSegment(
        chapter = genesisOne,
        startVerse = null,
        endVerse = null,
    )

    // A whole chapter reads its title first, so its verses run from 0 s to 6 s, 6 s to 10 s and 10 s to 14 s.
    private val audio = ChapterAudioModel(
        audioPath = "/cache/GEN_1.mp3",
        titleWords = emptyList(),
        verses = listOf(
            createAudioVerse(
                number = 1,
                start = 2.seconds,
                end = 5.seconds,
            ),
            createAudioVerse(
                number = 2,
                start = 6.seconds,
                end = 9.seconds,
            ),
            createAudioVerse(
                number = 3,
                start = 10.seconds,
                end = 13.seconds,
            ),
        ),
        duration = 14.seconds,
    )

    private lateinit var loader: ListeningSegmentLoader
    private lateinit var store: ChapterListeningStateStore
    private lateinit var playback: ListeningPlayback
    private lateinit var audioRepository: FakeChapterAudioRepository
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @Test
    fun `GIVEN a loaded chapter WHEN resetting to another chapter THEN returns the session left`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val leftSession = createSession(
                segment = wholeChapter,
                status = ListeningStatusModel.PLAYING,
            )
            store.update { it.copy(session = leftSession) }
            playback.load(
                audio = audio,
                start = playback.position,
            )

            // When
            val left = loader.reset(
                session = createSession(
                    segment = createSegment(
                        chapter = genesisTwo,
                        startVerse = null,
                        endVerse = null,
                    ),
                    status = ListeningStatusModel.PREPARING,
                ),
                status = ListeningStatusModel.PREPARING,
            )

            // Then
            assertEquals(leftSession, left)
        }

    @Test
    fun `GIVEN a loaded chapter WHEN resetting to the same chapter THEN no session is left`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val session = createSession(
                segment = wholeChapter,
                status = ListeningStatusModel.PLAYING,
            )
            store.update { it.copy(session = session) }
            playback.load(
                audio = audio,
                start = playback.position,
            )

            // When
            val left = loader.reset(
                session = session,
                status = ListeningStatusModel.PREPARING,
            )

            // Then
            assertNull(left)
        }

    @Test
    fun `GIVEN a chapter whose audio never loaded WHEN resetting to another chapter THEN no session is left`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            store.update {
                it.copy(
                    session = createSession(
                        segment = wholeChapter,
                        status = ListeningStatusModel.NEEDS_CONNECTION,
                    ),
                )
            }

            // When
            val left = loader.reset(
                session = createSession(
                    segment = createSegment(
                        chapter = genesisTwo,
                        startVerse = null,
                        endVerse = null,
                    ),
                    status = ListeningStatusModel.PREPARING,
                ),
                status = ListeningStatusModel.PREPARING,
            )

            // Then
            assertNull(left)
        }

    @Test
    fun `GIVEN no session WHEN resetting to a chapter THEN no session is left`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        playback.load(
            audio = audio,
            start = playback.position,
        )

        // When
        val left = loader.reset(
            session = createSession(
                segment = wholeChapter,
                status = ListeningStatusModel.PREPARING,
            ),
            status = ListeningStatusModel.PREPARING,
        )

        // Then
        assertNull(left)
    }

    @Test
    fun `GIVEN a loaded chapter WHEN resetting THEN shows the new session unloaded and drops the audio`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            store.update {
                it.copy(
                    session = createSession(
                        segment = wholeChapter,
                        status = ListeningStatusModel.PLAYING,
                    ),
                )
            }
            playback.load(
                audio = audio,
                start = playback.position,
            )
            val nextSession = createSession(
                segment = createSegment(
                    chapter = genesisTwo,
                    startVerse = null,
                    endVerse = null,
                ),
                status = ListeningStatusModel.PLAYING,
            )

            // When
            loader.reset(
                session = nextSession,
                status = ListeningStatusModel.PREPARING,
            )

            // Then
            assertEquals(nextSession.toUnloaded(ListeningStatusModel.PREPARING), store.session)
            assertNull(playback.audio)
        }

    @Test
    fun `GIVEN no connection WHEN loading THEN asks for a connection and tracks why`() = runTest(testDispatcher) {
        // Given
        prepareScenario(failure = ChapterAudioFailureModel.NO_CONNECTION)
        val session = createStoredSession(
            segment = wholeChapter,
            status = ListeningStatusModel.PREPARING,
        )

        // When
        val status = loader.load(
            session = session,
            leftSession = null,
        )

        // Then
        assertEquals(ListeningStatusModel.NEEDS_CONNECTION, status)
        assertEquals(ListeningStatusModel.NEEDS_CONNECTION, store.session?.status)
        assertTrue(
            store.session
                ?.verses
                .orEmpty()
                .isEmpty(),
        )
        assertNull(playback.audio)
        assertEquals("no_connection", trackedEvents.single().second["reason"])
    }

    @Test
    fun `GIVEN an audio without the segment verses WHEN loading THEN shows it unavailable and evicts it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val session = createStoredSession(
                segment = createSegment(
                    chapter = genesisOne,
                    startVerse = 7,
                    endVerse = 9,
                ),
                status = ListeningStatusModel.PREPARING,
            )

            // When
            val status = loader.load(
                session = session,
                leftSession = null,
            )

            // Then
            assertEquals(ListeningStatusModel.AUDIO_UNAVAILABLE, status)
            assertEquals(listOf(audio.audioPath), audioRepository.evicted)
            assertEquals("no_verses", trackedEvents.single().second["reason"])
        }

    @Test
    fun `GIVEN an empty audio already evicted WHEN loading it again THEN does not evict it again`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val session = createStoredSession(
                segment = createSegment(
                    chapter = genesisOne,
                    startVerse = 7,
                    endVerse = 9,
                ),
                status = ListeningStatusModel.PREPARING,
            )
            loader.load(
                session = session,
                leftSession = null,
            )

            // When
            loader.load(
                session = session,
                leftSession = null,
            )

            // Then
            assertEquals(listOf(audio.audioPath), audioRepository.evicted)
        }

    @Test
    fun `GIVEN a play pending WHEN loading THEN keeps it preparing with the audio from its first verse`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val session = createStoredSession(
                segment = wholeChapter,
                status = ListeningStatusModel.PREPARING,
            )

            // When
            val status = loader.load(
                session = session,
                leftSession = null,
            )

            // Then
            assertEquals(ListeningStatusModel.PREPARING, status)
            assertEquals(listOf(1, 2, 3), store.session?.verses?.map { it.number })
            assertEquals(audio, playback.audio)
            assertEquals(Duration.ZERO, playback.position)
        }

    @Test
    fun `GIVEN an interruption while loading WHEN loading THEN keeps it interrupted`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val session = createStoredSession(
            segment = wholeChapter,
            status = ListeningStatusModel.INTERRUPTED,
        )

        // When
        val status = loader.load(
            session = session,
            leftSession = null,
        )

        // Then
        assertEquals(ListeningStatusModel.INTERRUPTED, status)
    }

    @Test
    fun `GIVEN no play pending WHEN loading THEN shows it paused without tracking`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val session = createStoredSession(
            segment = wholeChapter,
            status = ListeningStatusModel.PLAYING,
        )

        // When
        val status = loader.load(
            session = session,
            leftSession = null,
        )

        // Then
        assertEquals(ListeningStatusModel.PAUSED, status)
        assertEquals(ListeningStatusModel.PAUSED, store.session?.status)
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a chapter left WHEN loading the next one THEN checks the revision of the one left`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val session = createStoredSession(
                segment = wholeChapter,
                status = ListeningStatusModel.PREPARING,
            )
            val leftSession = createSession(
                segment = createSegment(
                    chapter = genesisTwo,
                    startVerse = null,
                    endVerse = null,
                ),
                status = ListeningStatusModel.PLAYING,
            )

            // When
            loader.load(
                session = session,
                leftSession = leftSession,
            )

            // Then
            assertEquals(listOf(genesisTwo), audioRepository.refreshed)
        }

    @Test
    fun `GIVEN a segment in the middle of the chapter WHEN loading THEN starts at its first verse`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val session = createStoredSession(
                segment = createSegment(
                    chapter = genesisOne,
                    startVerse = 2,
                    endVerse = 3,
                ),
                status = ListeningStatusModel.PREPARING,
            )

            // When
            loader.load(
                session = session,
                leftSession = null,
            )

            // Then
            assertEquals(6.seconds, playback.position)
        }

    @Test
    fun `GIVEN a retry of the same segment WHEN loading THEN resumes where it was`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val session = createStoredSession(
            segment = wholeChapter,
            status = ListeningStatusModel.PREPARING,
        )
        playback.load(
            audio = playback.audio,
            start = 7.seconds,
        )
        loader.rememberRetryStart(wholeChapter)

        // When
        loader.load(
            session = session,
            leftSession = null,
        )

        // Then
        assertEquals(7.seconds, playback.position)
    }

    @Test
    fun `GIVEN a retry past the end of the segment WHEN loading THEN resumes at its end`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val session = createStoredSession(
            segment = wholeChapter,
            status = ListeningStatusModel.PREPARING,
        )
        playback.load(
            audio = playback.audio,
            start = 20.seconds,
        )
        loader.rememberRetryStart(wholeChapter)

        // When
        loader.load(
            session = session,
            leftSession = null,
        )

        // Then
        assertEquals(14.seconds, playback.position)
    }

    @Test
    fun `GIVEN a retry of another segment WHEN loading THEN starts at the first verse`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val session = createStoredSession(
            segment = wholeChapter,
            status = ListeningStatusModel.PREPARING,
        )
        playback.load(
            audio = playback.audio,
            start = 7.seconds,
        )
        loader.rememberRetryStart(
            createSegment(
                chapter = genesisOne,
                startVerse = 2,
                endVerse = 3,
            ),
        )

        // When
        loader.load(
            session = session,
            leftSession = null,
        )

        // Then
        assertEquals(Duration.ZERO, playback.position)
    }

    @Test
    fun `GIVEN a retry forgotten WHEN loading THEN starts at the first verse`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val session = createStoredSession(
            segment = wholeChapter,
            status = ListeningStatusModel.PREPARING,
        )
        playback.load(
            audio = playback.audio,
            start = 7.seconds,
        )
        loader.rememberRetryStart(wholeChapter)
        loader.forgetRetryStart()

        // When
        loader.load(
            session = session,
            leftSession = null,
        )

        // Then
        assertEquals(Duration.ZERO, playback.position)
    }

    @Test
    fun `GIVEN a retry already used WHEN loading again THEN starts at the first verse`() = runTest(testDispatcher) {
        // Given
        prepareScenario()
        val session = createStoredSession(
            segment = wholeChapter,
            status = ListeningStatusModel.PREPARING,
        )
        playback.load(
            audio = playback.audio,
            start = 7.seconds,
        )
        loader.rememberRetryStart(wholeChapter)
        loader.load(
            session = session,
            leftSession = null,
        )

        // When
        loader.load(
            session = session,
            leftSession = null,
        )

        // Then
        assertEquals(Duration.ZERO, playback.position)
    }

    private fun createStoredSession(
        segment: ListeningSegmentModel,
        status: ListeningStatusModel,
    ): ListeningSessionModel {
        val session = createSession(
            segment = segment,
            status = status,
        )
        store.update { it.copy(session = session) }
        return session
    }

    private fun createSession(
        segment: ListeningSegmentModel,
        status: ListeningStatusModel,
    ): ListeningSessionModel = createListeningSession(
        mode = ListeningModeModel.CHAPTER,
        day = null,
        segment = segment,
        bibleVersionId = "ACF",
        shouldForceCanonOrder = false,
    ).copy(status = status)

    private fun createSegment(
        chapter: ChapterLocationModel,
        startVerse: Int?,
        endVerse: Int?,
    ): ListeningSegmentModel = ListeningSegmentModel(
        chapter = chapter,
        startVerse = startVerse,
        endVerse = endVerse,
    )

    private fun createChapter(number: Int): ChapterLocationModel = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = number,
    )

    private fun createAudioVerse(
        number: Int,
        start: Duration,
        end: Duration,
    ): ChapterAudioVerseModel = ChapterAudioVerseModel(
        number = number,
        headingStart = null,
        headingWords = emptyList(),
        start = start,
        end = end,
        words = emptyList(),
    )

    private fun TestScope.prepareScenario(failure: ChapterAudioFailureModel? = null) {
        audioRepository = FakeChapterAudioRepository(mapOf(genesisOne to audio))
        audioRepository.failure = failure
        store = ChapterListeningStateStore()
        trackedEvents = mutableListOf()
        val tracker = ChapterListeningAnalyticsTracker { name, params -> trackedEvents += name to params }
        val prefetcher = ChapterAudioPrefetcher(
            scope = backgroundScope,
            chapterAudioRepository = audioRepository,
            findAdjacentSegment = FindAdjacentListeningSegmentUseCase { _, _, _ -> null },
            getChapterListeningAccess = { ChapterListeningAccessModel.Open },
        )
        playback = ListeningPlayback(
            scope = backgroundScope,
            audioPlayer = FakeChapterAudioPlayer(),
            mediaSession = FakeListeningMediaSession(canActivate = true),
            store = store,
            sleepTimer = ListeningSleepTimer(
                scope = backgroundScope,
                store = store,
                tracker = tracker,
            ),
            prefetcher = prefetcher,
            tracker = tracker,
            onSegmentEnd = {},
        )
        loader = ListeningSegmentLoader(
            store = store,
            playback = playback,
            prefetcher = prefetcher,
            cacheKeeper = ChapterAudioCacheKeeper(
                scope = backgroundScope,
                chapterAudioRepository = audioRepository,
            ),
            chapterAudioRepository = audioRepository,
            getListeningVerses = GetListeningVersesUseCase(
                getChapterVerseContents = {
                    mapOf(
                        1 to ChapterVerseContentModel(
                            text = "In the beginning",
                            heading = null,
                        ),
                    )
                },
                getListeningWords = GetListeningWordsUseCase(),
            ),
            tracker = tracker,
        )
    }
}
