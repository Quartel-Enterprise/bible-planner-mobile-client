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

    @Query("UPDATE days SET isRead = 0, readTimestamp = NULL")
    suspend fun resetAllDaysProgress()
}
