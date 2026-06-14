package com.quare.bibleplanner.core.plan.data.sync

import com.quare.bibleplanner.core.plan.data.dto.DayMetaDto
import com.quare.bibleplanner.core.plan.data.mapper.DayMetaMapper
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import kotlinx.coroutines.flow.Flow

/**
 * Adapts per-day metadata (read timestamp + notes) on the `days` table to the generic sync engine.
 * The day's read state is not synced here — it derives from chapter/verse read state.
 */
internal class DayMetaLocalStore(
    private val dayDao: DayDao,
    private val dayMetaMapper: DayMetaMapper,
) : SyncLocalStore<DayEntity, DayMetaDto> {
    override fun pendingFlow(): Flow<List<DayEntity>> = dayDao.getPendingDayMetaSyncFlow()

    override suspend fun getPending(): List<DayEntity> = dayDao.getPendingDayMetaSync()

    override suspend fun markSynced(entity: DayEntity) {
        entity.metaUpdatedAt?.let { syncedUpdatedAt ->
            dayDao.markDayMetaSynced(
                weekNumber = entity.weekNumber,
                dayNumber = entity.dayNumber,
                readingPlanType = entity.readingPlanType,
                syncedUpdatedAt = syncedUpdatedAt,
            )
        }
    }

    override suspend fun applyRemote(dto: DayMetaDto) {
        val remoteUpdatedAt = dayMetaMapper.toEpochMillis(dto.updatedAt)
        val updated = dayDao.applyRemoteDayMeta(
            weekNumber = dto.weekNumber,
            dayNumber = dto.dayNumber,
            readingPlanType = dto.planType,
            readTimestamp = dto.readTimestamp,
            notes = dto.notes,
            remoteUpdatedAt = remoteUpdatedAt,
        )
        // No row matched: insert it only when the day is genuinely absent (a 0 result can also mean the
        // local row is pending or newer, in which case Last-Write-Wins must keep it untouched).
        if (updated == 0 && dayDao.getDayByWeekAndDay(dto.weekNumber, dto.dayNumber, dto.planType) == null) {
            dayDao.insertDay(
                DayEntity(
                    weekNumber = dto.weekNumber,
                    dayNumber = dto.dayNumber,
                    readingPlanType = dto.planType,
                    isRead = false,
                    readTimestamp = dto.readTimestamp,
                    notes = dto.notes,
                    metaUpdatedAt = remoteUpdatedAt,
                    isMetaPendingSync = false,
                ),
            )
        }
    }

    override fun toDto(
        userId: String,
        entity: DayEntity,
    ): DayMetaDto = dayMetaMapper.toDto(
        userId = userId,
        entity = entity,
    )

    /** Marks pre-sync day metadata pending on first launch so it reaches the backend. */
    override suspend fun seed(now: Long) {
        dayDao.markLegacyDayMetaPending(now)
    }

    override suspend fun clearLocal() {
        dayDao.clearAllDayMetaSync()
    }
}
