package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.SaveVerseNoteUseCase
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseNoteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SaveVerseNoteUseCaseTest {
    private val now = 1_700_000_000_000L
    private val createdAt = 1_600_000_000_000L
    private lateinit var useCase: SaveVerseNoteUseCase
    private lateinit var repository: FakeVerseNoteRepository

    @Test
    fun `creates a note with the selected verses when there is none yet`() = runTest {
        // Given
        prepareScenario()

        // When
        useCase(
            noteId = null,
            bookId = BookId.GEN,
            chapterNumber = 3,
            verseNumbers = listOf(1, 2),
            text = "  Uma reflexao  ",
        )

        // Then
        val savedNote = repository.notes.value.single()
        assertEquals(
            expected = "Uma reflexao",
            actual = savedNote.text,
        )
        assertEquals(
            expected = listOf(1, 2),
            actual = savedNote.verseNumbers,
        )
        assertEquals(
            expected = now,
            actual = savedNote.createdAtEpochMillis,
        )
        assertTrue(savedNote.id.isNotBlank())
    }

    @Test
    fun `keeps the original creation time when editing an existing note`() = runTest {
        // Given
        prepareScenario(existingNote = existingNote())

        // When
        useCase(
            noteId = "note-1",
            bookId = BookId.GEN,
            chapterNumber = 3,
            verseNumbers = listOf(1, 2, 3),
            text = "Texto editado",
        )

        // Then
        val savedNote = repository.notes.value.single()
        assertEquals(
            expected = "note-1",
            actual = savedNote.id,
        )
        assertEquals(
            expected = createdAt,
            actual = savedNote.createdAtEpochMillis,
        )
        assertEquals(
            expected = now,
            actual = savedNote.updatedAtEpochMillis,
        )
        assertEquals(
            expected = listOf(1, 2, 3),
            actual = savedNote.verseNumbers,
        )
    }

    @Test
    fun `deletes the note when the text is cleared`() = runTest {
        // Given
        prepareScenario(existingNote = existingNote())

        // When
        useCase(
            noteId = "note-1",
            bookId = BookId.GEN,
            chapterNumber = 3,
            verseNumbers = listOf(1, 2),
            text = "   ",
        )

        // Then
        assertEquals(
            expected = listOf("note-1"),
            actual = repository.deletedNoteIds,
        )
        assertTrue(repository.notes.value.isEmpty())
    }

    @Test
    fun `does nothing when an empty note is saved without an existing one`() = runTest {
        // Given
        prepareScenario()

        // When
        useCase(
            noteId = null,
            bookId = BookId.GEN,
            chapterNumber = 3,
            verseNumbers = listOf(1),
            text = "",
        )

        // Then
        assertTrue(repository.notes.value.isEmpty())
        assertTrue(repository.deletedNoteIds.isEmpty())
    }

    private fun existingNote(): VerseNote = VerseNote(
        id = "note-1",
        bookId = BookId.GEN,
        chapterNumber = 3,
        verseNumbers = listOf(1, 2),
        text = "Texto original",
        createdAtEpochMillis = createdAt,
        updatedAtEpochMillis = createdAt,
    )

    private fun prepareScenario(existingNote: VerseNote? = null) {
        repository = FakeVerseNoteRepository(initialNotes = listOfNotNull(existingNote))
        useCase = SaveVerseNoteUseCase(
            verseNoteRepository = repository,
            currentTimestampProvider = { now },
        )
    }
}
