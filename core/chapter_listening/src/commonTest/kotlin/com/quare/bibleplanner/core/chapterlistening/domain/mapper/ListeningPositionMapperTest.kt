package com.quare.bibleplanner.core.chapterlistening.domain.mapper

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ListeningPositionMapperTest {
    // Verse 1 runs from 0 s to 4 s and verse 2 from 4 s to 8 s, each with two timed words and a pause between them.
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
                words = listOf(
                    createWord(
                        start = 0.5.seconds,
                        end = 1.seconds,
                    ),
                    createWord(
                        start = 1.2.seconds,
                        end = 2.seconds,
                    ),
                ),
            ),
            createVerse(
                number = 2,
                start = 4.seconds,
                end = 8.seconds,
                words = listOf(
                    createWord(
                        start = 4.5.seconds,
                        end = 5.seconds,
                    ),
                    createWord(
                        start = 5.5.seconds,
                        end = 6.seconds,
                    ),
                ),
            ),
        ),
    )

    @Test
    fun `GIVEN a position inside a verse WHEN applying it THEN shows the verse its progress and the spoken word`() {
        // Given
        val position = 4.8.seconds

        // When
        val positioned = session.withPosition(
            position = position,
            isWordHighlightEnabled = true,
        )

        // Then
        assertEquals(1, positioned.verseIndex)
        assertEquals(0.2f, positioned.verseProgress)
        assertEquals(0, positioned.wordIndex)
    }

    @Test
    fun `GIVEN a position in the pause between words WHEN applying it THEN highlights no word`() {
        // Given
        val position = 5.2.seconds

        // When
        val positioned = session.withPosition(
            position = position,
            isWordHighlightEnabled = true,
        )

        // Then
        assertEquals(1, positioned.verseIndex)
        assertNull(positioned.wordIndex)
    }

    @Test
    fun `GIVEN the word highlight turned off WHEN applying a position on a word THEN highlights no word`() {
        // Given
        val position = 4.8.seconds

        // When
        val positioned = session.withPosition(
            position = position,
            isWordHighlightEnabled = false,
        )

        // Then
        assertEquals(1, positioned.verseIndex)
        assertNull(positioned.wordIndex)
    }

    @Test
    fun `GIVEN a position before the first verse WHEN applying it THEN shows the first verse at its start`() {
        // Given
        val lateSession = session.copy(
            verses = listOf(
                createVerse(
                    number = 1,
                    start = 2.seconds,
                    end = 4.seconds,
                    words = emptyList(),
                ),
            ),
        )

        // When
        val positioned = lateSession.withPosition(
            position = 1.seconds,
            isWordHighlightEnabled = true,
        )

        // Then
        assertEquals(0, positioned.verseIndex)
        assertEquals(0f, positioned.verseProgress)
    }

    @Test
    fun `GIVEN a position after the last verse WHEN applying it THEN shows the last verse as complete`() {
        // Given
        val position = 9.seconds

        // When
        val positioned = session.withPosition(
            position = position,
            isWordHighlightEnabled = true,
        )

        // Then
        assertEquals(1, positioned.verseIndex)
        assertEquals(1f, positioned.verseProgress)
        assertNull(positioned.wordIndex)
    }

    @Test
    fun `GIVEN a verse with no length WHEN applying a position on it THEN its progress stays at zero`() {
        // Given
        val emptyVerseSession = session.copy(
            verses = listOf(
                createVerse(
                    number = 1,
                    start = 3.seconds,
                    end = 3.seconds,
                    words = emptyList(),
                ),
            ),
        )

        // When
        val positioned = emptyVerseSession.withPosition(
            position = 3.seconds,
            isWordHighlightEnabled = true,
        )

        // Then
        assertEquals(0f, positioned.verseProgress)
    }

    @Test
    fun `GIVEN a session without verses WHEN applying a position THEN keeps the session as it is`() {
        // Given
        val unloadedSession = session.copy(verses = emptyList())

        // When
        val positioned = unloadedSession.withPosition(
            position = 3.seconds,
            isWordHighlightEnabled = true,
        )

        // Then
        assertSame(unloadedSession, positioned)
    }

    @Test
    fun `GIVEN a verse shown WHEN updating the current word THEN finds the word in that verse only`() {
        // Given
        val shownSession = session.copy(
            verseIndex = 0,
            verseProgress = 0.5f,
        )

        // When
        val updated = shownSession.withCurrentWord(
            position = 1.5.seconds,
            isWordHighlightEnabled = true,
        )

        // Then
        assertEquals(1, updated.wordIndex)
        assertEquals(0, updated.verseIndex)
        assertEquals(0.5f, updated.verseProgress)
    }

    @Test
    fun `GIVEN the word highlight turned off WHEN updating the current word THEN highlights no word`() {
        // Given
        val highlightedSession = session.copy(
            verseIndex = 1,
            wordIndex = 0,
        )

        // When
        val updated = highlightedSession.withCurrentWord(
            position = 5.7.seconds,
            isWordHighlightEnabled = false,
        )

        // Then
        assertNull(updated.wordIndex)
    }

    @Test
    fun `GIVEN a session without verses WHEN updating the current word THEN highlights no word`() {
        // Given
        val unloadedSession = session.copy(
            verses = emptyList(),
            wordIndex = 1,
        )

        // When
        val updated = unloadedSession.withCurrentWord(
            position = 1.5.seconds,
            isWordHighlightEnabled = true,
        )

        // Then
        assertNull(updated.wordIndex)
    }

    @Test
    fun `GIVEN a position before a word WHEN finding the next word change THEN it is the word start`() {
        // Given
        val shownSession = session.copy(verseIndex = 1)

        // When
        val change = shownSession.findNextWordChange(4.2.seconds)

        // Then
        assertEquals(4.5.seconds, change)
    }

    @Test
    fun `GIVEN a position on a word WHEN finding the next word change THEN it is the word end`() {
        // Given
        val shownSession = session.copy(verseIndex = 1)

        // When
        val change = shownSession.findNextWordChange(4.7.seconds)

        // Then
        assertEquals(5.seconds, change)
    }

    @Test
    fun `GIVEN a position after a verse last word WHEN finding the next word change THEN it is the next verse start`() {
        // Given
        val shownSession = session.copy(verseIndex = 0)

        // When
        val change = shownSession.findNextWordChange(2.5.seconds)

        // Then
        assertEquals(4.5.seconds, change)
    }

    @Test
    fun `GIVEN a position after the final word WHEN finding the next word change THEN there is none`() {
        // Given
        val shownSession = session.copy(verseIndex = 1)

        // When
        val change = shownSession.findNextWordChange(6.5.seconds)

        // Then
        assertNull(change)
    }

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

    private fun createWord(
        start: Duration,
        end: Duration,
    ): ListeningWordModel = ListeningWordModel(
        target = ListeningWordTargetModel.Verse(0..1),
        start = start,
        end = end,
    )
}
