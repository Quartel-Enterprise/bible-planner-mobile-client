package com.quare.bibleplanner.core.books.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.data.dto.VersionDto
import com.quare.bibleplanner.core.books.data.mapper.CachedVersionsJsonMapper
import com.quare.bibleplanner.core.datastore.read
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

internal class BibleVersionsLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
    private val cachedVersionsJsonMapper: CachedVersionsJsonMapper,
) : BibleVersionsLocalDataSource {
    override suspend fun getCachedVersions(): List<VersionDto>? {
        val cachedData = dataStore.read(stringPreferencesKey(CACHE_KEY)) ?: return null
        return parseVersions(cachedData)
    }

    override fun observeCachedVersions(): Flow<List<VersionDto>?> = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(CACHE_KEY)]?.let(::parseVersions)
    }

    override suspend fun getCacheTimestamp(): Long? = dataStore.read(longPreferencesKey(TIMESTAMP_KEY))

    override suspend fun saveToCache(
        versions: List<VersionDto>,
        timestamp: Long,
    ) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(CACHE_KEY)] = json.encodeToString(versions)
            preferences[longPreferencesKey(TIMESTAMP_KEY)] = timestamp
        }
    }

    private fun parseVersions(cachedData: String): List<VersionDto>? = runCatching {
        cachedVersionsJsonMapper.map(cachedData)
    }.onFailure { Logger.e(it) { "Failed to parse cached Bible versions" } }
        .getOrNull()

    companion object {
        private const val CACHE_KEY = "bible_versions_cache"
        private const val TIMESTAMP_KEY = "bible_versions_timestamp"
    }
}
