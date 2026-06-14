package com.quare.bibleplanner.core.provider.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {
    @Query(
        "SELECT * FROM days WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber AND readingPlanType = :readingPlanType",
    )
    fun getDayByWeekAndDayFlow(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
    ): Flow<DayEntity?>

    @Query(
        "SELECT * FROM days WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber AND readingPlanType = :readingPlanType",
    )
    suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
    ): DayEntity?

    @Query("SELECT * FROM days WHERE weekNumber = :weekNumber AND readingPlanType = :readingPlanType")
    fun getDaysByWeekFlow(
        weekNumber: Int,
        readingPlanType: String,
    ): Flow<List<DayEntity>>

    @Query("SELECT * FROM days WHERE weekNumber = :weekNumber AND readingPlanType = :readingPlanType")
    suspend fun getDaysByWeek(
        weekNumber: Int,
        readingPlanType: String,
    ): List<DayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: DayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<DayEntity>): List<Long>

    @Update
    suspend fun updateDay(day: DayEntity)

    @Query(
        """
        UPDATE days 
        SET isRead = :isRead, readTimestamp = :readTimestamp 
        WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber AND readingPlanType = :readingPlanType
        """,
    )
    suspend fun updateDayReadStatus(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        isRead: Boolean,
        readTimestamp: Long?,
    )

    @Query(
        """
        UPDATE days 
        SET notes = :notes 
        WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber AND readingPlanType = :readingPlanType
        """,
    )
    suspend fun updateDayNotes(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        notes: String?,
    )

    @Query(
        "DELETE FROM days WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber AND readingPlanType = :readingPlanType",
    )
    suspend fun deleteDay(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
    )

    @Query("DELETE FROM days WHERE weekNumber = :weekNumber AND readingPlanType = :readingPlanType")
    suspend fun deleteDaysByWeek(
        weekNumber: Int,
        readingPlanType: String,
    )

    @Query("DELETE FROM days")
    suspend fun deleteAllDays()

    // region Day-meta sync (readTimestamp + notes; isRead derives from chapter/verse state)

    @Query("SELECT * FROM days WHERE isMetaPendingSync = 1")
    fun getPendingDayMetaSyncFlow(): Flow<List<DayEntity>>

    @Query("SELECT * FROM days WHERE isMetaPendingSync = 1")
    suspend fun getPendingDayMetaSync(): List<DayEntity>

    @Query(
        "UPDATE days SET isMetaPendingSync = 0 " +
            "WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber AND readingPlanType = :readingPlanType " +
            "AND metaUpdatedAt = :syncedUpdatedAt",
    )
    suspend fun markDayMetaSynced(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        syncedUpdatedAt: Long,
    )

    /** Applies a remote day-meta row with Last-Write-Wins. Returns the number of rows changed. */
    @Query(
        "UPDATE days SET readTimestamp = :readTimestamp, notes = :notes, metaUpdatedAt = :remoteUpdatedAt " +
            "WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber AND readingPlanType = :readingPlanType " +
            "AND isMetaPendingSync = 0 AND (metaUpdatedAt IS NULL OR metaUpdatedAt < :remoteUpdatedAt)",
    )
    suspend fun applyRemoteDayMeta(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        readTimestamp: Long?,
        notes: String?,
        remoteUpdatedAt: Long,
    ): Int

    /** Marks pre-sync day metadata pending on first launch so it reaches the backend. */
    @Query(
        "UPDATE days SET isMetaPendingSync = 1, metaUpdatedAt = :now " +
            "WHERE metaUpdatedAt IS NULL AND (readTimestamp IS NOT NULL OR (notes IS NOT NULL AND notes != ''))",
    )
    suspend fun markLegacyDayMetaPending(now: Long)

    /** Logout wipe: clears day progress, notes and sync metadata without scheduling a push. */
    @Query(
        "UPDATE days SET isRead = 0, readTimestamp = NULL, notes = NULL, metaUpdatedAt = NULL, isMetaPendingSync = 0",
    )
    suspend fun clearAllDayMetaSync()

    /**
     * Delete-progress wipe: clears day read state (keeps notes) and schedules a push for days that
     * already had a remote row (metaUpdatedAt not null), so the deletion propagates to other devices.
     */
    @Query(
        "UPDATE days SET isRead = 0, readTimestamp = NULL, " +
            "isMetaPendingSync = CASE WHEN metaUpdatedAt IS NOT NULL THEN 1 ELSE isMetaPendingSync END, " +
            "metaUpdatedAt = CASE WHEN metaUpdatedAt IS NOT NULL THEN :now ELSE metaUpdatedAt END",
    )
    suspend fun resetAllDayMetaForSync(now: Long)

    // endregion

    @Query(
        """
        SELECT COUNT(DISTINCT (weekNumber || '-' || dayNumber || '-' || readingPlanType))
        FROM days 
        WHERE notes IS NOT NULL AND notes != '' AND LENGTH(TRIM(notes)) > 0
        """,
    )
    suspend fun getDaysWithNotesCount(): Int
}
