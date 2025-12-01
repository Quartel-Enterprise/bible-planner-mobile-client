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
    @Query("SELECT * FROM days WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber")
    fun getDayByWeekAndDayFlow(
        weekNumber: Int,
        dayNumber: Int,
    ): Flow<DayEntity?>

    @Query("SELECT * FROM days WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber")
    suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
    ): DayEntity?

    @Query("SELECT * FROM days WHERE weekNumber = :weekNumber")
    fun getDaysByWeekFlow(weekNumber: Int): Flow<List<DayEntity>>

    @Query("SELECT * FROM days WHERE weekNumber = :weekNumber")
    suspend fun getDaysByWeek(weekNumber: Int): List<DayEntity>

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
        WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber
        """,
    )
    suspend fun updateDayReadStatus(
        weekNumber: Int,
        dayNumber: Int,
        isRead: Boolean,
        readTimestamp: Long?,
    )

    @Query("DELETE FROM days WHERE weekNumber = :weekNumber AND dayNumber = :dayNumber")
    suspend fun deleteDay(
        weekNumber: Int,
        dayNumber: Int,
    )

    @Query("DELETE FROM days WHERE weekNumber = :weekNumber")
    suspend fun deleteDaysByWeek(weekNumber: Int)

    @Query("DELETE FROM days")
    suspend fun deleteAllDays()

    @Query("UPDATE days SET isRead = 0, readTimestamp = NULL")
    suspend fun resetAllDaysProgress()
}
