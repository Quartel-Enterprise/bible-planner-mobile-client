package com.quare.bibleplanner.core.verseannotations.data.sync

import com.quare.bibleplanner.core.provider.room.dao.VerseHighlightDao
import com.quare.bibleplanner.core.provider.room.entity.VerseHighlightEntity
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import com.quare.bibleplanner.core.verseannotations.data.dto.VerseHighlightDto
import com.quare.bibleplanner.core.verseannotations.data.mapper.VerseHighlightMapper
import kotlinx.coroutines.flow.Flow

internal class VerseHighlightLocalStore(
    private val verseHighlightDao: VerseHighlightDao,
    private val verseHighlightMapper: VerseHighlightMapper,
) : SyncLocalStore<VerseHighlightEntity, VerseHighlightDto> {
    override fun observePending(): Flow<List<VerseHighlightEntity>> = verseHighlightDao.getPendingSyncHighlightsFlow()

    override suspend fun getPending(): List<VerseHighlightEntity> = verseHighlightDao.getPendingSyncHighlights()

    override suspend fun markSynced(entity: VerseHighlightEntity) {
        verseHighlightDao.markHighlightSynced(
            bibleVersionId = entity.bibleVersionId,
            bookId = entity.bookId,
            chapterNumber = entity.chapterNumber,
            verseNumber = entity.verseNumber,
            syncedUpdatedAt = entity.updatedAtEpochMillis,
        )
    }

    override suspend fun applyRemote(dto: VerseHighlightDto) {
        verseHighlightDao.applyRemoteHighlight(verseHighlightMapper.toEntity(dto))
    }

    override fun toDto(
        userId: String,
        entity: VerseHighlightEntity,
    ): VerseHighlightDto = verseHighlightMapper.toDto(
        userId = userId,
        entity = entity,
    )

    override suspend fun clearLocal() {
        verseHighlightDao.deleteAllHighlights()
    }
}
