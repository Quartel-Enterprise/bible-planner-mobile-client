package com.quare.bibleplanner.core.verseannotations.data.sync

import com.quare.bibleplanner.core.provider.room.entity.VerseNoteEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteVerseEntity
import com.quare.bibleplanner.core.provider.room.relation.VerseNoteWithVerses
import com.quare.bibleplanner.core.verseannotations.data.dto.VerseNoteDto
import com.quare.bibleplanner.core.verseannotations.data.mapper.SyncTimestampMapper
import com.quare.bibleplanner.core.verseannotations.data.mapper.VerseNoteSyncMapper
import com.quare.bibleplanner.core.verseannotations.fake.FakeVerseNoteDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class VerseNoteLocalStoreTest {
    private val remoteUpdatedAtMillis = Instant.parse(REMOTE_UPDATED_AT).toEpochMilliseconds()
    private lateinit var localStore: VerseNoteLocalStore
    private lateinit var dao: FakeVerseNoteDao

    @Test
    fun `GIVEN pending and synced notes WHEN observing and reading the pending ones THEN returns them with verses`() =
        runTest {
            // Given
            prepareScenario(
                initialNotes = listOf(
                    noteEntity(
                        id = "note-1",
                        isPendingSync = true,
                    ),
                    noteEntity(
                        id = "note-2",
                        isPendingSync = false,
                    ),
                ),
                initialVerses = listOf(
                    verse(
                        noteId = "note-1",
                        verseNumber = 3,
                        position = 0,
                    ),
                ),
            )

            // When
            val observed = localStore.observePending().first()
            val read = localStore.getPending()

            // Then
            val expected = listOf(
                VerseNoteWithVerses(
                    note = noteEntity(
                        id = "note-1",
                        isPendingSync = true,
                    ),
                    verses = listOf(
                        verse(
                            noteId = "note-1",
                            verseNumber = 3,
                            position = 0,
                        ),
                    ),
                ),
            )
            assertEquals(
                expected = expected,
                actual = observed,
            )
            assertEquals(
                expected = expected,
                actual = read,
            )
        }

    @Test
    fun `GIVEN a pushed note untouched meanwhile WHEN marking it synced THEN clears its pending flag`() = runTest {
        // Given
        val pending = noteEntity(
            id = "note-1",
            isPendingSync = true,
        )
        prepareScenario(initialNotes = listOf(pending))

        // When
        localStore.markSynced(
            VerseNoteWithVerses(
                note = pending,
                verses = emptyList(),
            ),
        )

        // Then
        assertEquals(
            expected = listOf(pending.copy(isPendingSync = false)),
            actual = dao.notes.value,
        )
    }

    @Test
    fun `GIVEN no local note WHEN applying a remote note THEN creates it with the remote passage`() = runTest {
        // Given
        prepareScenario()

        // When
        localStore.applyRemote(dto(verseNumbers = listOf(5, 4)))

        // Then
        assertEquals(
            expected = listOf(
                noteEntity(
                    id = "note-1",
                    createdAt = remoteUpdatedAtMillis,
                    updatedAt = remoteUpdatedAtMillis,
                ),
            ),
            actual = dao.notes.value,
        )
        assertEquals(
            expected = listOf(
                verse(
                    noteId = "note-1",
                    verseNumber = 5,
                    position = 0,
                ),
                verse(
                    noteId = "note-1",
                    verseNumber = 4,
                    position = 1,
                ),
            ),
            actual = dao.verses.value,
        )
    }

    @Test
    fun `GIVEN a synced note WHEN applying a newer remote edit that moves it THEN replaces the passage`() = runTest {
        // Given
        prepareScenario(
            initialNotes = listOf(noteEntity(id = "note-1")),
            initialVerses = listOf(
                verse(
                    noteId = "note-1",
                    verseNumber = 1,
                    position = 0,
                ),
            ),
        )

        // When
        localStore.applyRemote(dto(verseNumbers = listOf(2)))

        // Then
        assertEquals(
            expected = remoteUpdatedAtMillis,
            actual = dao.notes.value
                .single()
                .updatedAtEpochMillis,
        )
        assertEquals(
            expected = listOf(
                verse(
                    noteId = "note-1",
                    verseNumber = 2,
                    position = 0,
                ),
            ),
            actual = dao.verses.value,
        )
    }

    @Test
    fun `GIVEN a pending local note WHEN applying a remote edit THEN rejects it and keeps the local passage`() =
        runTest {
            // Given
            val pending = noteEntity(
                id = "note-1",
                isPendingSync = true,
            )
            val localVerse = verse(
                noteId = "note-1",
                verseNumber = 1,
                position = 0,
            )
            prepareScenario(
                initialNotes = listOf(pending),
                initialVerses = listOf(localVerse),
            )

            // When
            localStore.applyRemote(dto(verseNumbers = listOf(2)))

            // Then
            assertEquals(
                expected = listOf(pending),
                actual = dao.notes.value,
            )
            assertEquals(
                expected = listOf(localVerse),
                actual = dao.verses.value,
            )
        }

    @Test
    fun `GIVEN a local note WHEN building the remote payload THEN lists the verses in their stored order`() {
        // Given
        prepareScenario()

        // When
        val dto = localStore.toDto(
            userId = USER_ID,
            entity = VerseNoteWithVerses(
                note = noteEntity(
                    id = "note-1",
                    updatedAt = remoteUpdatedAtMillis,
                ),
                verses = listOf(
                    verse(
                        noteId = "note-1",
                        verseNumber = 4,
                        position = 1,
                    ),
                    verse(
                        noteId = "note-1",
                        verseNumber = 5,
                        position = 0,
                    ),
                ),
            ),
        )

        // Then
        assertEquals(
            expected = dto(verseNumbers = listOf(5, 4)),
            actual = dto,
        )
    }

    @Test
    fun `GIVEN a local note WHEN clearing the local store THEN removes every note`() = runTest {
        // Given
        prepareScenario(
            initialNotes = listOf(noteEntity(id = "note-1")),
            initialVerses = listOf(
                verse(
                    noteId = "note-1",
                    verseNumber = 1,
                    position = 0,
                ),
            ),
        )

        // When
        localStore.clearLocal()

        // Then
        assertTrue(dao.notes.value.isEmpty())
    }

    private fun dto(verseNumbers: List<Int>): VerseNoteDto = VerseNoteDto(
        userId = USER_ID,
        id = "note-1",
        bibleVersionId = "ACF",
        bookId = "ROM",
        chapterNumber = 8,
        verseNumbers = verseNumbers,
        text = "More than conquerors",
        isDeleted = false,
        updatedAt = REMOTE_UPDATED_AT,
    )

    private fun verse(
        noteId: String,
        verseNumber: Int,
        position: Int,
    ): VerseNoteVerseEntity = VerseNoteVerseEntity(
        noteId = noteId,
        verseNumber = verseNumber,
        position = position,
    )

    private fun noteEntity(
        id: String,
        isPendingSync: Boolean = false,
        createdAt: Long = LOCAL_UPDATED_AT,
        updatedAt: Long = LOCAL_UPDATED_AT,
    ): VerseNoteEntity = VerseNoteEntity(
        id = id,
        bibleVersionId = "ACF",
        bookId = "ROM",
        chapterNumber = 8,
        text = "More than conquerors",
        isDeleted = false,
        createdAtEpochMillis = createdAt,
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
        localStore = VerseNoteLocalStore(
            verseNoteDao = dao,
            verseNoteSyncMapper = VerseNoteSyncMapper(SyncTimestampMapper()),
        )
    }

    private companion object {
        const val USER_ID = "user-1"
        const val LOCAL_UPDATED_AT = 100L
        const val REMOTE_UPDATED_AT = "2026-07-11T10:00:00Z"
    }
}
