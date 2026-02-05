package com.quare.bibleplanner.feature.bibleversion.data.repository

import com.quare.bibleplanner.feature.bibleversion.data.datasource.BibleVersionsLocalDataSource
import com.quare.bibleplanner.feature.bibleversion.data.datasource.BibleVersionsRemoteDataSource
import com.quare.bibleplanner.feature.bibleversion.data.mapper.VersionMapper
import com.quare.bibleplanner.feature.bibleversion.domain.model.VersionModel
import com.quare.bibleplanner.feature.bibleversion.domain.repository.BibleVersionMetadataRepository
import kotlin.time.Clock

internal class BibleVersionMetadataRepositoryImpl(
    private val remoteDataSource: BibleVersionsRemoteDataSource,
    private val localDataSource: BibleVersionsLocalDataSource,
    private val versionMapper: VersionMapper,
) : BibleVersionMetadataRepository {
    override suspend fun getVersions(): Result<List<VersionModel>> {
        val cachedVersions = localDataSource.getCachedVersions()
        val timestamp = localDataSource.getCacheTimestamp() ?: 0L
        val now = Clock.System.now().toEpochMilliseconds()

        if (cachedVersions != null && (now - timestamp) < CACHE_DURATION_MS) {
            return Result.success(cachedVersions.map(versionMapper::map))
        }

        return remoteDataSource
            .getVersions()
            .map { remoteVersions ->
                if (remoteVersions.isNotEmpty()) {
                    localDataSource.saveToCache(remoteVersions, now)
                    remoteVersions.map(versionMapper::map)
                } else {
                    cachedVersions.orEmpty().map(versionMapper::map)
                }
            }.recover { error ->
                cachedVersions?.map(versionMapper::map) ?: throw error
            }
    }

    companion object {
        private const val CACHE_DURATION_MS = 4 * 60 * 60 * 1000L // 4 hours
    }
}
