package com.quare.bibleplanner.core.plan.data.sync

import com.quare.bibleplanner.core.plan.data.dto.DayMetaDto
import com.quare.bibleplanner.core.plan.data.mapper.DayMetaMapper
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import com.quare.bibleplanner.core.sync.domain.SyncLocalStore
import kotlinx.coroutines.flow.Flow

// Why: the day's read state is not synced here; it derives from chapter/verse read state.
internal class DayMetaLocalStore(
    private val dayDao: DayDao,
    private val dayMetaMapper: DayMetaMapper,
) : SyncLocalStore<DayEntity, DayMetaDto> {
    override fun observePending(): Flow<List<DayEntity>> = dayDao.getPendingDayMetaSyncFlow()

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
                    id = 0,
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

    override suspend fun seed(now: Long) {
        dayDao.markLegacyDayMetaPending(now)
    }

    override suspend fun clearLocal() {
        dayDao.clearAllDayMetaSync()
    }
}
