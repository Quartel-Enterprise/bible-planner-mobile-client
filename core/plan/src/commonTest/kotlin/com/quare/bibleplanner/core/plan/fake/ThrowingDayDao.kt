package com.quare.bibleplanner.core.plan.fake

import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import kotlinx.coroutines.flow.Flow

internal open class ThrowingDayDao : DayDao {
    override fun getDayByWeekAndDayFlow(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
    ): Flow<DayEntity?> = error("Unexpected call")

    override suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
    ): DayEntity? = error("Unexpected call")

    override fun getDaysByWeekFlow(
        weekNumber: Int,
        readingPlanType: String,
    ): Flow<List<DayEntity>> = error("Unexpected call")

    override suspend fun getDaysByWeek(
        weekNumber: Int,
        readingPlanType: String,
    ): List<DayEntity> = error("Unexpected call")

    override suspend fun insertDay(day: DayEntity): Long = error("Unexpected call")

    override suspend fun insertDays(days: List<DayEntity>): List<Long> = error("Unexpected call")

    override suspend fun updateDay(day: DayEntity) {
        error("Unexpected call")
    }

    override suspend fun updateDayReadStatus(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        isRead: Boolean,
        readTimestamp: Long?,
    ) {
        error("Unexpected call")
    }

    override suspend fun updateDayNotes(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        notes: String?,
    ) {
        error("Unexpected call")
    }

    override suspend fun deleteDay(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
    ) {
        error("Unexpected call")
    }

    override suspend fun deleteDaysByWeek(
        weekNumber: Int,
        readingPlanType: String,
    ) {
        error("Unexpected call")
    }

    override suspend fun deleteAllDays() {
        error("Unexpected call")
    }

    override fun getPendingDayMetaSyncFlow(): Flow<List<DayEntity>> = error("Unexpected call")

    override suspend fun getPendingDayMetaSync(): List<DayEntity> = error("Unexpected call")

    override suspend fun markDayMetaSynced(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        syncedUpdatedAt: Long,
    ) {
        error("Unexpected call")
    }

    override suspend fun applyRemoteDayMeta(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        readTimestamp: Long?,
        notes: String?,
        remoteUpdatedAt: Long,
    ): Int = error("Unexpected call")

    override suspend fun markLegacyDayMetaPending(now: Long) {
        error("Unexpected call")
    }

    override suspend fun clearAllDayMetaSync() {
        error("Unexpected call")
    }

    override suspend fun resetAllDayMetaForSync(now: Long) {
        error("Unexpected call")
    }

    override suspend fun getDaysWithNotesCount(): Int = error("Unexpected call")
}
