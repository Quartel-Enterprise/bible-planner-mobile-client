package com.quare.bibleplanner.core.verseannotations.data.sync

import com.quare.bibleplanner.core.provider.room.dao.VerseNoteDao
import com.quare.bibleplanner.core.provider.room.relation.VerseNoteWithVerses
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import com.quare.bibleplanner.core.verseannotations.data.dto.VerseNoteDto
import com.quare.bibleplanner.core.verseannotations.data.mapper.VerseNoteSyncMapper
import kotlinx.coroutines.flow.Flow

internal class VerseNoteLocalStore(
    private val verseNoteDao: VerseNoteDao,
    private val verseNoteSyncMapper: VerseNoteSyncMapper,
) : SyncLocalStore<VerseNoteWithVerses, VerseNoteDto> {
    override fun pendingFlow(): Flow<List<VerseNoteWithVerses>> = verseNoteDao.getPendingSyncNotesFlow()

    override suspend fun getPending(): List<VerseNoteWithVerses> = verseNoteDao.getPendingSyncNotes()

    override suspend fun markSynced(entity: VerseNoteWithVerses) {
        verseNoteDao.markNoteSynced(
            noteId = entity.note.id,
            syncedUpdatedAt = entity.note.updatedAtEpochMillis,
        )
    }

    override suspend fun applyRemote(dto: VerseNoteDto) {
        verseNoteDao.applyRemoteNote(
            note = verseNoteSyncMapper.toEntity(dto),
            verses = verseNoteSyncMapper.toVerseEntities(dto),
        )
    }

    override fun toDto(
        userId: String,
        entity: VerseNoteWithVerses,
    ): VerseNoteDto = verseNoteSyncMapper.toDto(
        userId = userId,
        relation = entity,
    )

    override suspend fun clearLocal() {
        verseNoteDao.deleteAllNotes()
    }
}
