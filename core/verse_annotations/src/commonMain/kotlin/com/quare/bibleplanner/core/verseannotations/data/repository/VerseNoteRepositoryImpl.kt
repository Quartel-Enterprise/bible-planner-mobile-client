package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.room.dao.VerseNoteDao
import com.quare.bibleplanner.core.verseannotations.data.mapper.VerseNoteEntityMapper
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class VerseNoteRepositoryImpl(
    private val verseNoteDao: VerseNoteDao,
    private val verseNoteEntityMapper: VerseNoteEntityMapper,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : VerseNoteRepository {
    override fun observeChapterNotes(chapter: ChapterRef): Flow<List<VerseNote>> = verseNoteDao
        .getChapterNotesFlow(
            bibleVersionId = chapter.bibleVersionId,
            bookId = chapter.bookId.name,
            chapterNumber = chapter.chapterNumber,
        ).map { relations -> relations.map(verseNoteEntityMapper::toDomain) }

    override suspend fun getNote(noteId: String): VerseNote? = verseNoteDao
        .getNoteById(noteId)
        ?.takeUnless { it.note.isDeleted }
        ?.let(verseNoteEntityMapper::toDomain)

    override suspend fun upsert(note: VerseNote) {
        val stamped = note.copy(updatedAtEpochMillis = currentTimestampProvider.getCurrentTimestamp())
        verseNoteDao.upsertNote(
            note = verseNoteEntityMapper.toEntity(
                note = stamped,
                isPendingSync = true,
            ),
            verses = verseNoteEntityMapper.toVerseEntities(stamped),
        )
    }

    override suspend fun delete(noteId: String) {
        verseNoteDao.markNoteDeleted(
            noteId = noteId,
            updatedAt = currentTimestampProvider.getCurrentTimestamp(),
        )
    }
}
