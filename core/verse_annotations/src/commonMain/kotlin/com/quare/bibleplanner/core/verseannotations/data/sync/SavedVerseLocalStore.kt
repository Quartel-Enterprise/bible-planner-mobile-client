package com.quare.bibleplanner.core.verseannotations.data.sync

import com.quare.bibleplanner.core.provider.room.dao.SavedVerseDao
import com.quare.bibleplanner.core.provider.room.entity.SavedVerseEntity
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import com.quare.bibleplanner.core.verseannotations.data.dto.SavedVerseDto
import com.quare.bibleplanner.core.verseannotations.data.mapper.SavedVerseMapper
import kotlinx.coroutines.flow.Flow

internal class SavedVerseLocalStore(
    private val savedVerseDao: SavedVerseDao,
    private val savedVerseMapper: SavedVerseMapper,
) : SyncLocalStore<SavedVerseEntity, SavedVerseDto> {
    override fun pendingFlow(): Flow<List<SavedVerseEntity>> = savedVerseDao.getPendingSyncSavedVersesFlow()

    override suspend fun getPending(): List<SavedVerseEntity> = savedVerseDao.getPendingSyncSavedVerses()

    override suspend fun markSynced(entity: SavedVerseEntity) {
        savedVerseDao.markSavedVerseSynced(
            bookId = entity.bookId,
            chapterNumber = entity.chapterNumber,
            verseNumber = entity.verseNumber,
            syncedUpdatedAt = entity.updatedAtEpochMillis,
        )
    }

    override suspend fun applyRemote(dto: SavedVerseDto) {
        savedVerseDao.applyRemoteSavedVerse(savedVerseMapper.toEntity(dto))
    }

    override fun toDto(
        userId: String,
        entity: SavedVerseEntity,
    ): SavedVerseDto = savedVerseMapper.toDto(
        userId = userId,
        entity = entity,
    )

    override suspend fun clearLocal() {
        savedVerseDao.deleteAllSavedVerses()
    }
}
