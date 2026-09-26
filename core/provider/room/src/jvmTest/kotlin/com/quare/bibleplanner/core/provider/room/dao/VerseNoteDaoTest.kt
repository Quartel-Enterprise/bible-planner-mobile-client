package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteVerseEntity
import com.quare.bibleplanner.core.provider.room.relation.VerseNoteWithVerses
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VerseNoteDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: VerseNoteDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.verseNoteDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN a note WHEN editing its passage THEN replaces the verses it covers`() = runTest {
        // Given
        dao.upsertNote(
            note = note(updatedAt = 10L),
            verses = verses(1, 2),
        )

        // When
        dao.upsertNote(
            note = note(updatedAt = 20L),
            verses = verses(5),
        )

        // Then
        assertEquals(
            expected = VerseNoteWithVerses(
                note = note(updatedAt = 20L),
                verses = verses(5),
            ),
            actual = dao.getNoteById(NOTE_ID),
        )
    }

    @Test
    fun `GIVEN notes WHEN observing a chapter THEN lists its live notes only`() = runTest {
        // Given
        dao.upsertNote(
            note = note(updatedAt = 10L),
            verses = verses(1),
        )
        dao.upsertNote(
            note = note(
                id = "deleted",
                updatedAt = 10L,
            ),
            verses = verses(2),
        )
        dao.markNoteDeleted(
            noteId = "deleted",
            updatedAt = 30L,
        )

        // When
        val notes = dao
            .getChapterNotesFlow(
                bibleVersionId = VERSION_ID,
                bookId = BOOK_ID,
                chapterNumber = CHAPTER,
            ).first()

        // Then
        assertEquals(
            expected = listOf(NOTE_ID),
            actual = notes.map { it.note.id },
        )
        assertEquals(
            expected = listOf(NOTE_ID, "deleted"),
            actual = dao.getPendingSyncNotesFlow().first().map { it.note.id },
        )
    }

    @Test
    fun `GIVEN a pending note WHEN marking it synced THEN it is no longer pending`() = runTest {
        // Given
        dao.upsertNote(
            note = note(updatedAt = 10L),
            verses = verses(1),
        )

        // When
        dao.markNoteSynced(
            noteId = NOTE_ID,
            syncedUpdatedAt = 10L,
        )

        // Then
        assertTrue(dao.getPendingSyncNotes().isEmpty())
    }

    @Test
    fun `GIVEN an unknown note WHEN applying a remote one THEN inserts it with its passage`() = runTest {
        // When
        dao.applyRemoteNote(
            note = note(
                updatedAt = 10L,
                isPendingSync = false,
            ),
            verses = verses(3, 4),
        )

        // Then
        assertEquals(
            expected = verses(3, 4),
            actual = dao.getNoteById(NOTE_ID)?.verses,
        )
    }

    @Test
    fun `GIVEN an older synced note WHEN applying a newer remote one THEN moves it to the remote passage`() = runTest {
        // Given
        dao.applyRemoteNote(
            note = note(
                updatedAt = 10L,
                isPendingSync = false,
            ),
            verses = verses(1),
        )

        // When
        dao.applyRemoteNote(
            note = note(
                updatedAt = 20L,
                isPendingSync = false,
                text = "Edited elsewhere",
            ),
            verses = verses(2),
        )

        // Then
        val stored = dao.getNoteById(NOTE_ID)
        assertEquals(
            expected = "Edited elsewhere",
            actual = stored?.note?.text,
        )
        assertEquals(
            expected = verses(2),
            actual = stored?.verses,
        )
    }

    @Test
    fun `GIVEN a pending local note WHEN applying a remote one THEN keeps the local passage`() = runTest {
        // Given
        dao.upsertNote(
            note = note(updatedAt = 10L),
            verses = verses(1),
        )

        // When
        dao.applyRemoteNote(
            note = note(
                updatedAt = 20L,
                isPendingSync = false,
                text = "Edited elsewhere",
            ),
            verses = verses(2),
        )

        // Then
        assertEquals(
            expected = VerseNoteWithVerses(
                note = note(updatedAt = 10L),
                verses = verses(1),
            ),
            actual = dao.getNoteById(NOTE_ID),
        )
    }

    @Test
    fun `GIVEN notes WHEN deleting all THEN none is left`() = runTest {
        // Given
        dao.upsertNote(
            note = note(updatedAt = 10L),
            verses = verses(1),
        )

        // When
        dao.deleteAllNotes()

        // Then
        assertTrue(dao.getPendingSyncNotes().isEmpty())
    }

    private fun verses(vararg numbers: Int): List<VerseNoteVerseEntity> = numbers.mapIndexed { index, number ->
        VerseNoteVerseEntity(
            noteId = NOTE_ID,
            verseNumber = number,
            position = index,
        )
    }

    private fun note(
        updatedAt: Long,
        id: String = NOTE_ID,
        isPendingSync: Boolean = true,
        text: String = "A note",
    ): VerseNoteEntity = VerseNoteEntity(
        id = id,
        bibleVersionId = VERSION_ID,
        bookId = BOOK_ID,
        chapterNumber = CHAPTER,
        text = text,
        isDeleted = false,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = updatedAt,
        isPendingSync = isPendingSync,
    )

    private companion object {
        const val NOTE_ID = "note-1"
        const val VERSION_ID = "ACF"
        const val BOOK_ID = "GEN"
        const val CHAPTER = 1
    }
}
