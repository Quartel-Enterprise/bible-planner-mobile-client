package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteVerseEntity
import com.quare.bibleplanner.core.provider.room.relation.VerseNoteWithVerses
import kotlinx.coroutines.flow.Flow

private const val INSERT_IGNORED = -1L

@Dao
interface VerseNoteDao {
    @Transaction
    @Query(
        "SELECT * FROM verse_notes WHERE bibleVersionId = :bibleVersionId AND bookId = :bookId " +
            "AND chapterNumber = :chapterNumber AND isDeleted = 0 ORDER BY createdAtEpochMillis",
    )
    fun getChapterNotesFlow(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
    ): Flow<List<VerseNoteWithVerses>>

    @Transaction
    @Query("SELECT * FROM verse_notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): VerseNoteWithVerses?

    @Transaction
    suspend fun upsertNote(
        note: VerseNoteEntity,
        verses: List<VerseNoteVerseEntity>,
    ) {
        upsertNote(note)
        deleteNoteVerses(note.id)
        insertNoteVerses(verses)
    }

    @Upsert
    suspend fun upsertNote(note: VerseNoteEntity)

    @Query(
        "UPDATE verse_notes SET isDeleted = 1, updatedAtEpochMillis = :updatedAt, isPendingSync = 1 " +
            "WHERE id = :noteId AND isDeleted = 0",
    )
    suspend fun markNoteDeleted(
        noteId: String,
        updatedAt: Long,
    )

    @Transaction
    @Query("SELECT * FROM verse_notes WHERE isPendingSync = 1")
    fun getPendingSyncNotesFlow(): Flow<List<VerseNoteWithVerses>>

    @Transaction
    @Query("SELECT * FROM verse_notes WHERE isPendingSync = 1")
    suspend fun getPendingSyncNotes(): List<VerseNoteWithVerses>

    @Query(
        "UPDATE verse_notes SET isPendingSync = 0 " +
            "WHERE id = :noteId AND updatedAtEpochMillis = :syncedUpdatedAt",
    )
    suspend fun markNoteSynced(
        noteId: String,
        syncedUpdatedAt: Long,
    )

    /**
     * Last-Write-Wins for the note row, and the verses it covers are replaced only when that row
     * actually moved — otherwise a rejected remote update would still overwrite the local passage.
     */
    @Transaction
    suspend fun applyRemoteNote(
        note: VerseNoteEntity,
        verses: List<VerseNoteVerseEntity>,
    ) {
        val insertedRowId = insertNoteIfAbsent(note)
        val changedRows = if (insertedRowId == INSERT_IGNORED) {
            updateRemoteNote(
                noteId = note.id,
                bibleVersionId = note.bibleVersionId,
                bookId = note.bookId,
                chapterNumber = note.chapterNumber,
                text = note.text,
                isDeleted = note.isDeleted,
                remoteUpdatedAt = note.updatedAtEpochMillis,
            )
        } else {
            1
        }
        if (changedRows > 0) {
            deleteNoteVerses(note.id)
            insertNoteVerses(verses)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteIfAbsent(note: VerseNoteEntity): Long

    @Query(
        "UPDATE verse_notes SET bibleVersionId = :bibleVersionId, bookId = :bookId, " +
            "chapterNumber = :chapterNumber, text = :text, isDeleted = :isDeleted, " +
            "updatedAtEpochMillis = :remoteUpdatedAt " +
            "WHERE id = :noteId AND isPendingSync = 0 AND updatedAtEpochMillis < :remoteUpdatedAt",
    )
    suspend fun updateRemoteNote(
        noteId: String,
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
        text: String,
        isDeleted: Boolean,
        remoteUpdatedAt: Long,
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteVerses(verses: List<VerseNoteVerseEntity>)

    @Query("DELETE FROM verse_note_verses WHERE noteId = :noteId")
    suspend fun deleteNoteVerses(noteId: String)

    @Query("DELETE FROM verse_notes")
    suspend fun deleteAllNotes()
}
