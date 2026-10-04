package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.factory.AnnotatedPassageFactory
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ObserveAnnotatedPassagesUseCase
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.RemovePassageAnnotationsUseCase
import com.quare.bibleplanner.core.verseannotations.fake.FakeSavedVerseRepository
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseHighlightRepository
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseNoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AnnotatedPassageUseCasesTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ARC",
        bookId = BookId.JHN,
        chapterNumber = 3,
    )
    private val yellow = HighlightColor.Preset(PresetHighlightColor.YELLOW)
    private val testNote = VerseNote(
        id = "note-1",
        chapter = testChapter,
        verseNumbers = listOf(16),
        text = "Amor",
        createdAtEpochMillis = 0L,
        updatedAtEpochMillis = 0L,
    )
    private lateinit var highlightRepository: FakeVerseHighlightRepository
    private lateinit var savedVerseRepository: FakeSavedVerseRepository
    private lateinit var noteRepository: FakeVerseNoteRepository

    @BeforeTest
    fun setUp() {
        highlightRepository = FakeVerseHighlightRepository(
            initialColors = mapOf(
                verseRef(16) to yellow,
                verseRef(17) to yellow,
                verseRef(1).copy(chapter = testChapter.copy(bibleVersionId = "WEB")) to yellow,
            ),
        )
        savedVerseRepository = FakeSavedVerseRepository(initialSavedRefs = setOf(verseRef(17)))
        noteRepository = FakeVerseNoteRepository(initialNotes = listOf(testNote))
    }

    @Test
    fun `GIVEN annotations in two versions WHEN observing the passages of one THEN emits only its passages`() =
        runTest {
            // Given
            val useCase = ObserveAnnotatedPassagesUseCase(
                verseHighlightRepository = highlightRepository,
                savedVerseRepository = savedVerseRepository,
                verseNoteRepository = noteRepository,
                annotatedPassageFactory = AnnotatedPassageFactory(),
            )

            // When
            val passages = useCase(testChapter.bibleVersionId).first()

            // Then
            assertEquals(
                expected = setOf(
                    AnnotatedPassage(
                        chapter = testChapter,
                        verseNumbers = listOf(16),
                        highlightColor = yellow,
                        isSaved = false,
                        note = testNote,
                        updatedAtEpochMillis = 0L,
                    ),
                    AnnotatedPassage(
                        chapter = testChapter,
                        verseNumbers = listOf(17),
                        highlightColor = yellow,
                        isSaved = true,
                        note = null,
                        updatedAtEpochMillis = 0L,
                    ),
                ),
                actual = passages.toSet(),
            )
        }

    @Test
    fun `GIVEN a fully annotated passage WHEN removing its annotations THEN removes highlight bookmark and note`() =
        runTest {
            // Given
            val useCase = RemovePassageAnnotationsUseCase(
                verseHighlightRepository = highlightRepository,
                savedVerseRepository = savedVerseRepository,
                verseNoteRepository = noteRepository,
            )

            // When
            useCase(
                AnnotatedPassage(
                    chapter = testChapter,
                    verseNumbers = listOf(16, 17),
                    highlightColor = yellow,
                    isSaved = true,
                    note = testNote,
                    updatedAtEpochMillis = 0L,
                ),
            )

            // Then
            assertEquals(
                expected = setOf(testChapter.copy(bibleVersionId = "WEB")),
                actual = highlightRepository.colors.value.keys
                    .map { it.chapter }
                    .toSet(),
            )
            assertEquals(
                expected = emptySet(),
                actual = savedVerseRepository.savedRefs.value,
            )
            assertEquals(
                expected = listOf("note-1"),
                actual = noteRepository.deletedNoteIds,
            )
        }

    @Test
    fun `GIVEN a passage carrying only a bookmark WHEN removing its annotations THEN leaves the rest untouched`() =
        runTest {
            // Given
            val useCase = RemovePassageAnnotationsUseCase(
                verseHighlightRepository = highlightRepository,
                savedVerseRepository = savedVerseRepository,
                verseNoteRepository = noteRepository,
            )

            // When
            useCase(
                AnnotatedPassage(
                    chapter = testChapter,
                    verseNumbers = listOf(17),
                    highlightColor = null,
                    isSaved = true,
                    note = null,
                    updatedAtEpochMillis = 0L,
                ),
            )

            // Then
            assertEquals(
                expected = yellow,
                actual = highlightRepository.colors.value[verseRef(17)],
            )
            assertEquals(
                expected = emptySet(),
                actual = savedVerseRepository.savedRefs.value,
            )
            assertEquals(
                expected = emptyList(),
                actual = noteRepository.deletedNoteIds,
            )
        }

    private fun verseRef(verseNumber: Int): VerseRef = VerseRef(
        chapter = testChapter,
        verseNumber = verseNumber,
    )
}
