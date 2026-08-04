package com.quare.bibleplanner.core.books.data.datasource

import com.quare.bibleplanner.core.books.data.dto.VersionDto

internal interface BibleVersionsLocalDataSource {
    suspend fun getCachedVersions(): List<VersionDto>?

    suspend fun getCacheTimestamp(): Long?

    suspend fun saveToCache(
        versions: List<VersionDto>,
        timestamp: Long,
    )
}
