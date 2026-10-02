package com.quare.bibleplanner.core.chapterstudy.data.datasource

import com.quare.bibleplanner.core.provider.room.dao.ChapterStudyDao
import com.quare.bibleplanner.core.provider.room.relation.ChapterStudyWithContent

internal class ChapterStudyLocalDataSource(
    private val chapterStudyDao: ChapterStudyDao,
) {
    suspend fun findByCacheKey(cacheKey: String): ChapterStudyWithContent? = chapterStudyDao.getByCacheKey(cacheKey)

    suspend fun save(content: ChapterStudyWithContent) {
        chapterStudyDao.replace(content)
    }

    suspend fun deleteByCacheKey(cacheKey: String) {
        chapterStudyDao.deleteByCacheKey(cacheKey)
    }

    suspend fun deleteAll() {
        chapterStudyDao.deleteAll()
    }
}
