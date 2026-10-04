package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ObserveChapterAnnotationsUseCase
import com.quare.bibleplanner.core.verseannotations.fake.FakeSavedVerseRepository
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseHighlightRepository
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseNoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveChapterAnnotationsUseCaseTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private val yellow = HighlightColor.Preset(PresetHighlightColor.YELLOW)
    private lateinit var useCase: ObserveChapterAnnotationsUseCase

    @Test
    fun `GIVEN a chapter with annotations WHEN observing it THEN merges its highlights saved verses and notes`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val annotations = useCase(
                chapter = testChapter,
            ).first()

            // Then
            assertEquals(
                expected = mapOf(1 to yellow),
                actual = annotations.highlightColorByVerse,
            )
            assertEquals(
                expected = setOf(2),
                actual = annotations.savedVerseNumbers,
            )
            assertEquals(
                expected = mapOf(4 to "note-1", 5 to "note-1"),
                actual = annotations.noteIdByVerse,
            )
            assertEquals(
                expected = mapOf("note-1" to listOf(4, 5)),
                actual = annotations.noteVerseNumbersById,
            )
        }

    @Test
    fun `GIVEN annotations in a chapter WHEN observing another chapter THEN ignores them`() = runTest {
        // Given
        prepareScenario()

        // When
        val annotations = useCase(testChapter.copy(chapterNumber = 4)).first()

        // Then
        assertEquals(
            expected = emptyMap(),
            actual = annotations.highlightColorByVerse,
        )
        assertEquals(
            expected = emptySet(),
            actual = annotations.savedVerseNumbers,
        )
        assertEquals(
            expected = emptyMap(),
            actual = annotations.noteIdByVerse,
        )
    }

    @Test
    fun `GIVEN annotations in a version WHEN observing the chapter in another version THEN leaves them out`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val annotations = useCase(testChapter.copy(bibleVersionId = "WEB")).first()

            // Then
            assertEquals(
                expected = emptyMap(),
                actual = annotations.highlightColorByVerse,
            )
            assertEquals(
                expected = emptySet(),
                actual = annotations.savedVerseNumbers,
            )
            assertEquals(
                expected = emptyMap(),
                actual = annotations.noteIdByVerse,
            )
        }

    private fun verseRef(verseNumber: Int): VerseRef = VerseRef(
        chapter = testChapter,
        verseNumber = verseNumber,
    )

    private fun prepareScenario() {
        useCase = ObserveChapterAnnotationsUseCase(
            verseHighlightRepository = FakeVerseHighlightRepository(
                initialColors = mapOf(verseRef(1) to yellow),
            ),
            savedVerseRepository = FakeSavedVerseRepository(initialSavedRefs = setOf(verseRef(2))),
            verseNoteRepository = FakeVerseNoteRepository(
                initialNotes = listOf(
                    VerseNote(
                        id = "note-1",
                        chapter = testChapter,
                        verseNumbers = listOf(4, 5),
                        text = "Reflexao",
                        createdAtEpochMillis = 0L,
                        updatedAtEpochMillis = 0L,
                    ),
                ),
            ),
        )
    }
}
