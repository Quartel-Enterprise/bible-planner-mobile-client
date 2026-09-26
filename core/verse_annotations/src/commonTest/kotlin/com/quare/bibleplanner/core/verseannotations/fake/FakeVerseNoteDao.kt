package com.quare.bibleplanner.core.verseannotations.fake

import com.quare.bibleplanner.core.provider.room.dao.VerseNoteDao
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseNoteVerseEntity
import com.quare.bibleplanner.core.provider.room.relation.VerseNoteWithVerses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

internal class FakeVerseNoteDao(
    initialNotes: List<VerseNoteEntity> = emptyList(),
    initialVerses: List<VerseNoteVerseEntity> = emptyList(),
) : VerseNoteDao {
    val notes = MutableStateFlow(initialNotes)
    val verses = MutableStateFlow(initialVerses)

    override fun getChapterNotesFlow(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
    ): Flow<List<VerseNoteWithVerses>> = combine(notes, verses) { currentNotes, currentVerses ->
        currentNotes
            .filter { it.bibleVersionId == bibleVersionId && it.bookId == bookId && it.chapterNumber == chapterNumber }
            .filterNot { it.isDeleted }
            .sortedBy { it.createdAtEpochMillis }
            .map { note -> note.withVerses(currentVerses) }
    }

    override suspend fun getNoteById(noteId: String): VerseNoteWithVerses? =
        notes.value.find { it.id == noteId }?.withVerses(verses.value)

    override suspend fun upsertNote(note: VerseNoteEntity) {
        notes.value = notes.value.filterNot { it.id == note.id } + note
    }

    override suspend fun markNoteDeleted(
        noteId: String,
        updatedAt: Long,
    ) {
        notes.value = notes.value.map { note ->
            if (note.id == noteId && !note.isDeleted) {
                note.copy(
                    isDeleted = true,
                    updatedAtEpochMillis = updatedAt,
                    isPendingSync = true,
                )
            } else {
                note
            }
        }
    }

    override fun getPendingSyncNotesFlow(): Flow<List<VerseNoteWithVerses>> =
        combine(notes, verses) { currentNotes, currentVerses ->
            currentNotes.filter { it.isPendingSync }.map { note -> note.withVerses(currentVerses) }
        }

    override suspend fun getPendingSyncNotes(): List<VerseNoteWithVerses> =
        notes.value.filter { it.isPendingSync }.map { note -> note.withVerses(verses.value) }

    override suspend fun markNoteSynced(
        noteId: String,
        syncedUpdatedAt: Long,
    ) {
        notes.value = notes.value.map { note ->
            if (note.id == noteId &&
                note.updatedAtEpochMillis == syncedUpdatedAt
            ) {
                note.copy(isPendingSync = false)
            } else {
                note
            }
        }
    }

    override suspend fun insertNoteIfAbsent(note: VerseNoteEntity): Long {
        if (notes.value.any { it.id == note.id }) return INSERT_IGNORED
        notes.value = notes.value + note
        return notes.value.size.toLong()
    }

    override suspend fun updateRemoteNote(
        noteId: String,
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
        text: String,
        isDeleted: Boolean,
        remoteUpdatedAt: Long,
    ): Int {
        var changedRows = 0
        notes.value = notes.value.map { note ->
            if (note.id == noteId && !note.isPendingSync && note.updatedAtEpochMillis < remoteUpdatedAt) {
                changedRows++
                note.copy(
                    bibleVersionId = bibleVersionId,
                    bookId = bookId,
                    chapterNumber = chapterNumber,
                    text = text,
                    isDeleted = isDeleted,
                    updatedAtEpochMillis = remoteUpdatedAt,
                )
            } else {
                note
            }
        }
        return changedRows
    }

    override suspend fun insertNoteVerses(verses: List<VerseNoteVerseEntity>) {
        this.verses.value = this.verses.value.filterNot { current ->
            verses.any { it.noteId == current.noteId && it.verseNumber == current.verseNumber }
        } + verses
    }

    override suspend fun deleteNoteVerses(noteId: String) {
        verses.value = verses.value.filterNot { it.noteId == noteId }
    }

    override suspend fun deleteAllNotes() {
        notes.value = emptyList()
        verses.value = emptyList()
    }

    private fun VerseNoteEntity.withVerses(allVerses: List<VerseNoteVerseEntity>): VerseNoteWithVerses =
        VerseNoteWithVerses(
            note = this,
            verses = allVerses.filter { it.noteId == id },
        )

    private companion object {
        const val INSERT_IGNORED = -1L
    }
}
