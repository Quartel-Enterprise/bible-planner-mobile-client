package com.quare.bibleplanner.core.books.data.sync

import com.quare.bibleplanner.core.books.data.dto.VerseReadDto
import com.quare.bibleplanner.core.books.data.mapper.VerseReadMapper
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.relation.PendingVerseRead
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import kotlinx.coroutines.flow.Flow

/**
 * Adapts verse-range read state on the `verses` table to the generic sync engine. Only verses read as
 * part of a verse range carry sync metadata; whole-chapter reads sync at chapter level
 * ([ChapterReadLocalStore]). The pending rows are projected with their stable cross-device identity
 * (book id + chapter number + verse number) via [PendingVerseRead].
 */
internal class VerseReadLocalStore(
    private val verseDao: VerseDao,
    private val verseReadMapper: VerseReadMapper,
) : SyncLocalStore<PendingVerseRead, VerseReadDto> {
    override fun pendingFlow(): Flow<List<PendingVerseRead>> = verseDao.getPendingReadSyncVersesFlow()

    override suspend fun getPending(): List<PendingVerseRead> = verseDao.getPendingReadSyncVerses()

    override suspend fun markSynced(entity: PendingVerseRead) {
        entity.readUpdatedAt?.let { syncedUpdatedAt ->
            verseDao.markVerseReadSynced(
                bookId = entity.bookId,
                chapterNumber = entity.chapterNumber,
                verseNumber = entity.verseNumber,
                syncedUpdatedAt = syncedUpdatedAt,
            )
        }
    }

    override suspend fun applyRemote(dto: VerseReadDto) {
        verseDao.applyRemoteVerseRead(
            bookId = dto.bookId,
            chapterNumber = dto.chapterNumber,
            verseNumber = dto.verseNumber,
            isRead = dto.isRead,
            remoteUpdatedAt = verseReadMapper.toEpochMillis(dto.updatedAt),
        )
    }

    override fun toDto(
        userId: String,
        entity: PendingVerseRead,
    ): VerseReadDto = verseReadMapper.toDto(
        userId = userId,
        entity = entity,
    )

    /** Marks pre-sync verse-range reads pending on first launch so they reach the backend. */
    override suspend fun seed(now: Long) {
        verseDao.markLegacyVerseReadsPending(now)
    }

    override suspend fun clearLocal() {
        verseDao.clearAllVerseReadSync()
    }
}
