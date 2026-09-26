package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteVerseEntity
import com.quare.bibleplanner.core.verseannotations.data.mapper.VerseNoteEntityMapper
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseNoteDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class VerseNoteRepositoryImplTest {
    private val testChapter = ChapterRef(
        bibleVersionId = "ACF",
        bookId = BookId.PSA,
        chapterNumber = 23,
    )
    private lateinit var repository: VerseNoteRepositoryImpl
    private lateinit var dao: FakeVerseNoteDao

    @Test
    fun `observes the live notes of the chapter with their verses in order`() = runTest {
        // Given
        prepareScenario(
            initialNotes = listOf(
                noteEntity(id = "note-1"),
                noteEntity(
                    id = "note-2",
                    isDeleted = true,
                ),
                noteEntity(
                    id = "note-3",
                    chapterNumber = 24,
                ),
            ),
            initialVerses = listOf(
                VerseNoteVerseEntity(
                    noteId = "note-1",
                    verseNumber = 4,
                    position = 1,
                ),
                VerseNoteVerseEntity(
                    noteId = "note-1",
                    verseNumber = 1,
                    position = 0,
                ),
            ),
        )

        // When
        val notes = repository.observeChapterNotes(testChapter).first()

        // Then
        assertEquals(
            expected = listOf(
                note(
                    id = "note-1",
                    verseNumbers = listOf(1, 4),
                ),
            ),
            actual = notes,
        )
    }

    @Test
    fun `gets a note by its id`() = runTest {
        // Given
        prepareScenario(
            initialNotes = listOf(noteEntity(id = "note-1")),
            initialVerses = listOf(
                VerseNoteVerseEntity(
                    noteId = "note-1",
                    verseNumber = 2,
                    position = 0,
                ),
            ),
        )

        // When
        val note = repository.getNote("note-1")

        // Then
        assertEquals(
            expected = note(
                id = "note-1",
                verseNumbers = listOf(2),
            ),
            actual = note,
        )
    }

    @Test
    fun `does not return a deleted note`() = runTest {
        // Given
        prepareScenario(
            initialNotes = listOf(
                noteEntity(
                    id = "note-1",
                    isDeleted = true,
                ),
            ),
        )

        // When
        val note = repository.getNote("note-1")

        // Then
        assertNull(note)
    }

    @Test
    fun `does not return a note that does not exist`() = runTest {
        // Given
        prepareScenario()

        // When
        val note = repository.getNote("missing")

        // Then
        assertNull(note)
    }

    @Test
    fun `stores a note stamped with the current time as pending with its verses in order`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.upsert(
            note(
                id = "note-1",
                verseNumbers = listOf(3, 5),
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                noteEntity(
                    id = "note-1",
                    updatedAt = NOW,
                    isPendingSync = true,
                ),
            ),
            actual = dao.notes.value,
        )
        assertEquals(
            expected = listOf(
                VerseNoteVerseEntity(
                    noteId = "note-1",
                    verseNumber = 3,
                    position = 0,
                ),
                VerseNoteVerseEntity(
                    noteId = "note-1",
                    verseNumber = 5,
                    position = 1,
                ),
            ),
            actual = dao.verses.value,
        )
    }

    @Test
    fun `editing a note replaces the verses it covers`() = runTest {
        // Given
        prepareScenario(
            initialNotes = listOf(noteEntity(id = "note-1")),
            initialVerses = listOf(
                VerseNoteVerseEntity(
                    noteId = "note-1",
                    verseNumber = 1,
                    position = 0,
                ),
            ),
        )

        // When
        repository.upsert(
            note(
                id = "note-1",
                verseNumbers = listOf(2),
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                VerseNoteVerseEntity(
                    noteId = "note-1",
                    verseNumber = 2,
                    position = 0,
                ),
            ),
            actual = dao.verses.value,
        )
    }

    @Test
    fun `deleting a note soft deletes it as a pending change`() = runTest {
        // Given
        prepareScenario(initialNotes = listOf(noteEntity(id = "note-1")))

        // When
        repository.delete("note-1")

        // Then
        assertEquals(
            expected = listOf(
                noteEntity(
                    id = "note-1",
                    isDeleted = true,
                    updatedAt = NOW,
                    isPendingSync = true,
                ),
            ),
            actual = dao.notes.value,
        )
    }

    private fun note(
        id: String,
        verseNumbers: List<Int>,
    ): VerseNote = VerseNote(
        id = id,
        chapter = testChapter,
        verseNumbers = verseNumbers,
        text = "The Lord is my shepherd",
        createdAtEpochMillis = CREATED_AT,
        updatedAtEpochMillis = OLD_TIMESTAMP,
    )

    private fun noteEntity(
        id: String,
        isDeleted: Boolean = false,
        chapterNumber: Int = testChapter.chapterNumber,
        updatedAt: Long = OLD_TIMESTAMP,
        isPendingSync: Boolean = false,
    ): VerseNoteEntity = VerseNoteEntity(
        id = id,
        bibleVersionId = testChapter.bibleVersionId,
        bookId = testChapter.bookId.name,
        chapterNumber = chapterNumber,
        text = "The Lord is my shepherd",
        isDeleted = isDeleted,
        createdAtEpochMillis = CREATED_AT,
        updatedAtEpochMillis = updatedAt,
        isPendingSync = isPendingSync,
    )

    private fun prepareScenario(
        initialNotes: List<VerseNoteEntity> = emptyList(),
        initialVerses: List<VerseNoteVerseEntity> = emptyList(),
    ) {
        dao = FakeVerseNoteDao(
            initialNotes = initialNotes,
            initialVerses = initialVerses,
        )
        repository = VerseNoteRepositoryImpl(
            verseNoteDao = dao,
            verseNoteEntityMapper = VerseNoteEntityMapper(),
            currentTimestampProvider = { NOW },
        )
    }

    private companion object {
        const val CREATED_AT = 10L
        const val OLD_TIMESTAMP = 100L
        const val NOW = 5_000L
    }
}
