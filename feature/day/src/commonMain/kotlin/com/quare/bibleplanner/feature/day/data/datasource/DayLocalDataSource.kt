package com.quare.bibleplanner.feature.day.data.datasource

import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import kotlinx.coroutines.flow.Flow

class DayLocalDataSource(
    private val dayDao: DayDao,
) {
    fun getDayByWeekAndDayFlow(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
    ): Flow<DayEntity?> = dayDao.getDayByWeekAndDayFlow(weekNumber, dayNumber, readingPlanType)

    suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
    ): DayEntity? = dayDao.getDayByWeekAndDay(weekNumber, dayNumber, readingPlanType)

    suspend fun updateDayReadStatus(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        isRead: Boolean,
        readTimestamp: Long?,
    ) {
        val existingDay = dayDao.getDayByWeekAndDay(weekNumber, dayNumber, readingPlanType)
        if (existingDay != null) {
            // Use @Update annotation instead of custom query to ensure Room Flow emits
            dayDao.updateDay(
                existingDay.copy(
                    isRead = isRead,
                    readTimestamp = readTimestamp,
                ),
            )
        } else {
            dayDao.insertDay(
                DayEntity(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                    readingPlanType = readingPlanType,
                    isRead = isRead,
                    readTimestamp = readTimestamp,
                    notes = null,
                ),
            )
        }
    }

    suspend fun updateDayNotes(
        weekNumber: Int,
        dayNumber: Int,
        readingPlanType: String,
        notes: String?,
    ) {
        dayDao.run {
            val existingDay = getDayByWeekAndDay(weekNumber, dayNumber, readingPlanType)
            if (existingDay != null) {
                updateDay(
                    existingDay.copy(notes = notes),
                )
            } else {
                insertDay(
                    DayEntity(
                        weekNumber = weekNumber,
                        dayNumber = dayNumber,
                        readingPlanType = readingPlanType,
                        isRead = false,
                        readTimestamp = null,
                        notes = notes,
                    ),
                )
            }
        }
    }

    suspend fun getDaysWithNotesCount(): Int = dayDao.getDaysWithNotesCount()
}
