package com.quare.bibleplanner.core.books.data.sync

import com.quare.bibleplanner.core.books.data.dto.ChapterReadDto
import com.quare.bibleplanner.core.books.data.mapper.ChapterReadMapper
import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import kotlinx.coroutines.flow.Flow

internal class ChapterReadLocalStore(
    private val chapterDao: ChapterDao,
    private val verseDao: VerseDao,
    private val chapterReadMapper: ChapterReadMapper,
) : SyncLocalStore<ChapterEntity, ChapterReadDto> {
    override fun observePending(): Flow<List<ChapterEntity>> = chapterDao.getPendingReadSyncChaptersFlow()

    override suspend fun getPending(): List<ChapterEntity> = chapterDao.getPendingReadSyncChapters()

    override suspend fun markSynced(entity: ChapterEntity) {
        entity.readUpdatedAt?.let { syncedUpdatedAt ->
            chapterDao.markChapterReadSynced(
                bookId = entity.bookId,
                chapterNumber = entity.number,
                syncedUpdatedAt = syncedUpdatedAt,
            )
        }
    }

    override suspend fun applyRemote(dto: ChapterReadDto) {
        val changed = chapterDao.applyRemoteChapterRead(
            bookId = dto.bookId,
            chapterNumber = dto.chapterNumber,
            isRead = dto.isRead,
            remoteUpdatedAt = chapterReadMapper.toEpochMillis(dto.updatedAt),
        )
        if (changed > 0) {
            verseDao.cascadeChapterReadToVerses(
                bookId = dto.bookId,
                chapterNumber = dto.chapterNumber,
                isRead = dto.isRead,
            )
        }
    }

    override fun toDto(
        userId: String,
        entity: ChapterEntity,
    ): ChapterReadDto = chapterReadMapper.toDto(
        userId = userId,
        entity = entity,
    )

    override suspend fun seed(now: Long) {
        chapterDao.markLegacyChapterReadsPending(now)
    }

    override suspend fun clearLocal() {
        chapterDao.clearAllChapterReadSync()
    }
}
