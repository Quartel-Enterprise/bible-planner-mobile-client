package com.quare.bibleplanner.feature.day.data.datasource

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.DayDao
import com.quare.bibleplanner.core.provider.room.entity.DayEntity
import kotlinx.coroutines.flow.Flow

class DayLocalDataSource(
    private val dayDao: DayDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
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
        val metaUpdatedAt = currentTimestampProvider.getCurrentTimestamp()
        val existingDay = dayDao.getDayByWeekAndDay(weekNumber, dayNumber, readingPlanType)
        if (existingDay != null) {
            // Use @Update annotation instead of custom query to ensure Room Flow emits
            dayDao.updateDay(
                existingDay.copy(
                    isRead = isRead,
                    readTimestamp = readTimestamp,
                    metaUpdatedAt = metaUpdatedAt,
                    isMetaPendingSync = true,
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
                    metaUpdatedAt = metaUpdatedAt,
                    isMetaPendingSync = true,
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
        val metaUpdatedAt = currentTimestampProvider.getCurrentTimestamp()
        dayDao.run {
            val existingDay = getDayByWeekAndDay(weekNumber, dayNumber, readingPlanType)
            if (existingDay != null) {
                updateDay(
                    existingDay.copy(
                        notes = notes,
                        metaUpdatedAt = metaUpdatedAt,
                        isMetaPendingSync = true,
                    ),
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
                        metaUpdatedAt = metaUpdatedAt,
                        isMetaPendingSync = true,
                    ),
                )
            }
        }
    }

    suspend fun getDaysWithNotesCount(): Int = dayDao.getDaysWithNotesCount()
}
