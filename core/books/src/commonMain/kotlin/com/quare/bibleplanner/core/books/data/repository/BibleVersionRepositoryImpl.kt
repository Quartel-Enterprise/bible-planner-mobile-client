package com.quare.bibleplanner.core.books.data.repository

import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsLocalDataSource
import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsRemoteDataSource
import com.quare.bibleplanner.core.books.data.mapper.VersionMapper
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import kotlin.Result
import kotlin.collections.List
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.orEmpty
import kotlin.map
import kotlin.recover
import kotlin.time.Clock

internal class BibleVersionRepositoryImpl(
    private val remoteDataSource: BibleVersionsRemoteDataSource,
    private val localDataSource: BibleVersionsLocalDataSource,
    private val versionMapper: VersionMapper,
) : BibleVersionRepository {
    override suspend fun getVersions(): Result<List<VersionModel>> {
        val cachedVersions = localDataSource.getCachedVersions()
        val timestamp = localDataSource.getCacheTimestamp() ?: 0L
        val now = Clock.System.now().toEpochMilliseconds()

        return if (cachedVersions != null && (now - timestamp) < CACHE_DURATION_MS) {
            Result.success(cachedVersions.map(versionMapper::map))
        } else {
            remoteDataSource
                .getVersions()
                .map { remoteVersions ->
                    if (remoteVersions.isNotEmpty()) {
                        localDataSource.saveToCache(remoteVersions, now)
                        remoteVersions.map(versionMapper::map)
                    } else {
                        cachedVersions.orEmpty().map(versionMapper::map)
                    }
                }.recoverCatching { error ->
                    cachedVersions?.map(versionMapper::map) ?: throw error
                }
        }
    }

    companion object {
        private const val CACHE_DURATION_MS = 4 * 60 * 60 * 1000L // 4 hours
    }
}
