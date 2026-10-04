package com.quare.bibleplanner.core.verseannotations.domain.factory

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.SavedVerse
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseHighlight
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AnnotatedPassageFactoryTest {
    private val genesis = ChapterRef(
        bibleVersionId = "ARC",
        bookId = BookId.GEN,
        chapterNumber = 1,
    )
    private val psalms = genesis.copy(
        bookId = BookId.PSA,
        chapterNumber = 23,
    )
    private val yellow = HighlightColor.Preset(PresetHighlightColor.YELLOW)
    private val green = HighlightColor.Preset(PresetHighlightColor.GREEN)
    private val factory = AnnotatedPassageFactory()

    @Test
    fun `GIVEN consecutive verses marked the same way WHEN creating the passages THEN joins them into one passage`() {
        // Given
        val highlights = listOf(
            highlight(verseNumber = 1),
            highlight(verseNumber = 2),
            highlight(
                verseNumber = 3,
                color = green,
            ),
            highlight(verseNumber = 5),
        )

        // When
        val passages = factory.create(
            highlights = highlights,
            savedVerses = emptyList(),
            notes = emptyList(),
        )

        // Then
        assertEquals(
            expected = listOf(
                listOf(1, 2) to yellow,
                listOf(3) to green,
                listOf(5) to yellow,
            ),
            actual = passages.map { it.verseNumbers to it.highlightColor },
        )
    }

    @Test
    fun `GIVEN a highlight and a bookmark on one verse WHEN creating the passages THEN keeps them in one passage`() {
        // Given
        val highlights = listOf(
            highlight(
                verseNumber = 1,
                updatedAt = 10L,
            ),
        )
        val savedVerses = listOf(
            savedVerse(
                verseNumber = 1,
                updatedAt = 20L,
            ),
        )

        // When
        val passages = factory.create(
            highlights = highlights,
            savedVerses = savedVerses,
            notes = emptyList(),
        )

        // Then
        assertEquals(
            expected = listOf(
                AnnotatedPassage(
                    chapter = genesis,
                    verseNumbers = listOf(1),
                    highlightColor = yellow,
                    isSaved = true,
                    note = null,
                    updatedAtEpochMillis = 20L,
                ),
            ),
            actual = passages,
        )
    }

    @Test
    fun `GIVEN a highlighted run where the bookmark changes WHEN creating the passages THEN splits the run there`() {
        // Given
        val highlights = listOf(
            highlight(verseNumber = 1),
            highlight(verseNumber = 2),
        )
        val savedVerses = listOf(savedVerse(verseNumber = 2))

        // When
        val passages = factory.create(
            highlights = highlights,
            savedVerses = savedVerses,
            notes = emptyList(),
        )

        // Then
        assertEquals(
            expected = listOf(
                listOf(1) to false,
                listOf(2) to true,
            ),
            actual = passages.map { it.verseNumbers to it.isSaved },
        )
    }

    @Test
    fun `GIVEN uniform marks on the verses of a note WHEN creating the passages THEN folds them into the note`() {
        // Given
        val note = note(
            verseNumbers = listOf(6, 5),
            updatedAt = 30L,
        )

        // When
        val passages = factory.create(
            highlights = listOf(
                highlight(
                    verseNumber = 5,
                    updatedAt = 40L,
                ),
                highlight(verseNumber = 6),
            ),
            savedVerses = emptyList(),
            notes = listOf(note),
        )

        // Then
        assertEquals(
            expected = listOf(
                AnnotatedPassage(
                    chapter = genesis,
                    verseNumbers = listOf(5, 6),
                    highlightColor = yellow,
                    isSaved = false,
                    note = note,
                    updatedAtEpochMillis = 40L,
                ),
            ),
            actual = passages,
        )
    }

    @Test
    fun `GIVEN marks that differ across the verses of a note WHEN creating the passages THEN keeps the note apart`() {
        // Given
        val note = note(
            verseNumbers = listOf(5, 6),
            updatedAt = 30L,
        )

        // When
        val passages = factory.create(
            highlights = listOf(
                highlight(
                    verseNumber = 5,
                    updatedAt = 10L,
                ),
            ),
            savedVerses = emptyList(),
            notes = listOf(note),
        )

        // Then
        assertEquals(
            expected = listOf(
                AnnotatedPassage(
                    chapter = genesis,
                    verseNumbers = listOf(5, 6),
                    highlightColor = null,
                    isSaved = false,
                    note = note,
                    updatedAtEpochMillis = 30L,
                ),
                AnnotatedPassage(
                    chapter = genesis,
                    verseNumbers = listOf(5),
                    highlightColor = yellow,
                    isSaved = false,
                    note = null,
                    updatedAtEpochMillis = 10L,
                ),
            ),
            actual = passages,
        )
    }

    @Test
    fun `GIVEN two notes on the same verse WHEN creating the passages THEN only the first note claims its marks`() {
        // Given
        val first = note(
            id = "first",
            verseNumbers = listOf(1),
            createdAt = 1L,
        )
        val second = note(
            id = "second",
            verseNumbers = listOf(1),
            createdAt = 2L,
        )

        // When
        val passages = factory.create(
            highlights = listOf(highlight(verseNumber = 1)),
            savedVerses = emptyList(),
            notes = listOf(second, first),
        )

        // Then
        assertEquals(
            expected = listOf(
                "first" to yellow,
                "second" to null,
            ),
            actual = passages.map { it.note?.id to it.highlightColor },
        )
    }

    @Test
    fun `GIVEN highlights at different times WHEN creating passages THEN orders by recency then canonical position`() {
        // Given
        val highlights = listOf(
            highlight(
                verseNumber = 1,
                chapter = psalms,
                updatedAt = 10L,
            ),
            highlight(
                verseNumber = 3,
                updatedAt = 10L,
            ),
            highlight(
                verseNumber = 9,
                updatedAt = 50L,
            ),
        )

        // When
        val passages = factory.create(
            highlights = highlights,
            savedVerses = emptyList(),
            notes = emptyList(),
        )

        // Then
        assertEquals(
            expected = listOf(
                genesis to listOf(9),
                genesis to listOf(3),
                psalms to listOf(1),
            ),
            actual = passages.map { it.chapter to it.verseNumbers },
        )
    }

    private fun highlight(
        verseNumber: Int,
        color: HighlightColor = yellow,
        chapter: ChapterRef = genesis,
        updatedAt: Long = 0L,
    ): VerseHighlight = VerseHighlight(
        ref = VerseRef(
            chapter = chapter,
            verseNumber = verseNumber,
        ),
        color = color,
        updatedAtEpochMillis = updatedAt,
    )

    private fun savedVerse(
        verseNumber: Int,
        updatedAt: Long = 0L,
    ): SavedVerse = SavedVerse(
        ref = VerseRef(
            chapter = genesis,
            verseNumber = verseNumber,
        ),
        updatedAtEpochMillis = updatedAt,
    )

    private fun note(
        verseNumbers: List<Int>,
        id: String = "note",
        createdAt: Long = 0L,
        updatedAt: Long = 0L,
    ): VerseNote = VerseNote(
        id = id,
        chapter = genesis,
        verseNumbers = verseNumbers,
        text = "Reflexão",
        createdAtEpochMillis = createdAt,
        updatedAtEpochMillis = updatedAt,
    )
}
