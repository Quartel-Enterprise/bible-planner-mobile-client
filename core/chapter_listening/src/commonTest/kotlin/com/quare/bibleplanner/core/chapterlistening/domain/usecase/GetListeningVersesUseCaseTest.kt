package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.ChapterVerseContentModel
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.createListeningSession
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioWordModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.book.ChapterRef
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class GetListeningVersesUseCaseTest {
    private val genesisOne = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = 1,
    )
    private val contents = mapOf(
        1 to ChapterVerseContentModel(
            text = "In the beginning",
            heading = null,
        ),
        2 to ChapterVerseContentModel(
            text = "And the earth",
            heading = "The light",
        ),
        3 to ChapterVerseContentModel(
            text = "And God said",
            heading = null,
        ),
    )

    // The title takes the first 2 s and a heading with timed words is read before verse 2.
    private val audio = ChapterAudioModel(
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
            createAudioVerse(
                number = 1,
                headingStart = null,
                start = 2.seconds,
                end = 5.5.seconds,
            ),
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
            ),
            createAudioVerse(
                number = 3,
                headingStart = null,
                start = 10.5.seconds,
                end = 13.seconds,
            ),
        ),
        duration = 14.seconds,
    )

    private lateinit var useCase: GetListeningVersesUseCase
    private lateinit var requestedChapters: MutableList<ChapterRef>

    @Test
    fun `GIVEN a whole chapter WHEN getting its verses THEN spans from the title to the audio end`() = runTest {
        // Given
        prepareScenario()

        // When
        val verses = useCase(
            audio = audio,
            session = createSession(
                startVerse = null,
                endVerse = null,
            ),
        )

        // Then
        assertEquals(listOf(Duration.ZERO, 6.seconds, 10.5.seconds), verses.map { it.start })
        assertEquals(listOf(6.seconds, 10.5.seconds, 14.seconds), verses.map { it.end })
    }

    @Test
    fun `GIVEN a whole chapter WHEN getting its verses THEN reads their text in the session version`() = runTest {
        // Given
        prepareScenario()

        // When
        val verses = useCase(
            audio = audio,
            session = createSession(
                startVerse = null,
                endVerse = null,
            ),
        )

        // Then
        assertEquals(listOf("In the beginning", "And the earth", "And God said"), verses.map { it.text })
        assertEquals(
            listOf(
                ChapterRef(
                    bibleVersionId = BIBLE_VERSION_ID,
                    bookId = BookId.GEN,
                    chapterNumber = 1,
                ),
            ),
            requestedChapters,
        )
    }

    @Test
    fun `GIVEN a whole chapter WHEN getting its verses THEN only the first verse holds the title words`() = runTest {
        // Given
        prepareScenario()

        // When
        val verses = useCase(
            audio = audio,
            session = createSession(
                startVerse = null,
                endVerse = null,
            ),
        )

        // Then
        assertEquals(
            listOf(ListeningWordTargetModel.ChapterBook, ListeningWordTargetModel.ChapterNumber),
            verses.first().words.map { it.target },
        )
        val laterTargets = verses.drop(1).flatMap { verse -> verse.words.map { it.target } }
        assertFalse(ListeningWordTargetModel.ChapterBook in laterTargets)
        assertFalse(ListeningWordTargetModel.ChapterNumber in laterTargets)
    }

    @Test
    fun `GIVEN a verse read after a heading WHEN getting the verses THEN highlights the heading words`() = runTest {
        // Given
        prepareScenario()

        // When
        val verses = useCase(
            audio = audio,
            session = createSession(
                startVerse = null,
                endVerse = null,
            ),
        )

        // Then
        assertEquals(
            listOf(
                ListeningWordTargetModel.Heading(0 until 3),
                ListeningWordTargetModel.Heading(4 until 9),
            ),
            verses[1].words.map { it.target },
        )
    }

    @Test
    fun `GIVEN a middle segment WHEN getting its verses THEN spans from its heading to its verse end`() = runTest {
        // Given
        prepareScenario()

        // When
        val verses = useCase(
            audio = audio,
            session = createSession(
                startVerse = 2,
                endVerse = 2,
            ),
        )

        // Then
        assertEquals(listOf(2), verses.map { it.number })
        assertEquals(6.seconds, verses.single().start)
        assertEquals(10.seconds, verses.single().end)
    }

    @Test
    fun `GIVEN a middle segment WHEN getting its verses THEN leaves the title words out`() = runTest {
        // Given
        prepareScenario()

        // When
        val verses = useCase(
            audio = audio,
            session = createSession(
                startVerse = 2,
                endVerse = 3,
            ),
        )

        // Then
        assertEquals(
            listOf(
                ListeningWordTargetModel.Heading(0 until 3),
                ListeningWordTargetModel.Heading(4 until 9),
            ),
            verses.first().words.map { it.target },
        )
    }

    @Test
    fun `GIVEN a segment from verse 1 WHEN getting its verses THEN spans from the title to its last verse`() = runTest {
        // Given
        prepareScenario()

        // When
        val verses = useCase(
            audio = audio,
            session = createSession(
                startVerse = 1,
                endVerse = 2,
            ),
        )

        // Then
        assertEquals(listOf(Duration.ZERO, 6.seconds), verses.map { it.start })
        assertEquals(listOf(6.seconds, 10.seconds), verses.map { it.end })
    }

    @Test
    fun `GIVEN the chapter text cannot be read WHEN getting the verses THEN keeps the timed verses without text`() =
        runTest {
            // Given
            prepareScenario(isReadingFailing = true)

            // When
            val verses = useCase(
                audio = audio,
                session = createSession(
                    startVerse = null,
                    endVerse = null,
                ),
            )

            // Then
            assertEquals(listOf(1, 2, 3), verses.map { it.number })
            assertEquals(listOf("", "", ""), verses.map { it.text })
        }

    private fun createSession(
        startVerse: Int?,
        endVerse: Int?,
    ): ListeningSessionModel = createListeningSession(
        mode = ListeningModeModel.CHAPTER,
        day = null,
        segment = ListeningSegmentModel(
            chapter = genesisOne,
            startVerse = startVerse,
            endVerse = endVerse,
        ),
        bibleVersionId = BIBLE_VERSION_ID,
        shouldForceCanonOrder = false,
    )

    private fun createAudioVerse(
        number: Int,
        headingStart: Duration?,
        start: Duration,
        end: Duration,
        headingWords: List<ChapterAudioWordModel> = emptyList(),
    ): ChapterAudioVerseModel = ChapterAudioVerseModel(
        number = number,
        headingStart = headingStart,
        headingWords = headingWords,
        start = start,
        end = end,
        words = emptyList(),
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

    private fun prepareScenario(isReadingFailing: Boolean = false) {
        requestedChapters = mutableListOf()
        useCase = GetListeningVersesUseCase(
            getChapterVerseContents = { chapter ->
                requestedChapters += chapter
                if (isReadingFailing) error("Bible not downloaded")
                contents
            },
            getListeningWords = GetListeningWordsUseCase(),
        )
    }

    private companion object {
        const val BIBLE_VERSION_ID = "ACF"
    }
}
