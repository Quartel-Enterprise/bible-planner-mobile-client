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
    ): Flow<DayEntity?> = dayDao.getDayByWeekAndDayFlow(weekNumber, dayNumber)

    suspend fun getDayByWeekAndDay(
        weekNumber: Int,
        dayNumber: Int,
    ): DayEntity? = dayDao.getDayByWeekAndDay(weekNumber, dayNumber)

    suspend fun updateDayReadStatus(
        weekNumber: Int,
        dayNumber: Int,
        isRead: Boolean,
        readTimestamp: Long?,
    ) {
        val existingDay = dayDao.getDayByWeekAndDay(weekNumber, dayNumber)
        if (existingDay != null) {
            dayDao.updateDayReadStatus(weekNumber, dayNumber, isRead, readTimestamp)
        } else {
            dayDao.insertDay(
                DayEntity(
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                    isRead = isRead,
                    readTimestamp = readTimestamp,
                ),
            )
        }
    }
}
