package com.quare.bibleplanner.feature.daystudy.data.datasource

import com.quare.bibleplanner.core.provider.room.dao.DayStudyDao
import com.quare.bibleplanner.core.provider.room.relation.DayStudyWithContent

internal class DayStudyLocalDataSource(
    private val dayStudyDao: DayStudyDao,
) {
    suspend fun getByCacheKey(cacheKey: String): DayStudyWithContent? = dayStudyDao.getByCacheKey(cacheKey)

    suspend fun save(content: DayStudyWithContent) {
        dayStudyDao.replace(content)
    }

    suspend fun deleteByCacheKey(cacheKey: String) {
        dayStudyDao.deleteByCacheKey(cacheKey)
    }

    suspend fun deleteAll() {
        dayStudyDao.deleteAll()
    }
}
