package com.quare.bibleplanner.core.books.data.repository

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsLocalDataSource
import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsRemoteDataSource
import com.quare.bibleplanner.core.books.data.mapper.VersionMapper
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class BibleVersionRepositoryImpl(
    private val remoteDataSource: BibleVersionsRemoteDataSource,
    private val localDataSource: BibleVersionsLocalDataSource,
    private val versionMapper: VersionMapper,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : BibleVersionRepository {
    private val cacheDuration: Duration = 15.minutes

    override suspend fun getVersions(forceRefresh: Boolean): Result<List<VersionModel>> {
        val cachedVersions = localDataSource.getCachedVersions()
        val timestamp = localDataSource.getCacheTimestamp() ?: 0L
        val now = currentTimestampProvider.getCurrentTimestamp()
        val cacheAge = (now - timestamp).milliseconds
        val isCacheFresh = cacheAge >= Duration.ZERO && cacheAge < cacheDuration

        return if (!forceRefresh && cachedVersions != null && isCacheFresh) {
            Result.success(cachedVersions.map(versionMapper::map))
        } else {
            remoteDataSource
                .getVersions()
                .map { remoteVersions ->
                    if (remoteVersions.isNotEmpty()) {
                        localDataSource.saveToCache(
                            versions = remoteVersions,
                            timestamp = now,
                        )
                        remoteVersions.map(versionMapper::map)
                    } else {
                        Logger.w { "The remote listing returned no Bible versions; keeping the cached ones" }
                        cachedVersions.orEmpty().map(versionMapper::map)
                    }
                }.recoverCatching { error ->
                    cachedVersions?.map(versionMapper::map) ?: throw error
                }
        }
    }

    override fun observeVersions(): Flow<List<VersionModel>> = flow {
        localDataSource.getCachedVersions()?.let { cached -> emit(cached.map(versionMapper::map)) }
        emit(getVersions(forceRefresh = false).getOrDefault(emptyList()))
        emitAll(
            localDataSource
                .observeCachedVersions()
                .filterNotNull()
                .map { versions -> versions.map(versionMapper::map) },
        )
    }.distinctUntilChanged()
}
