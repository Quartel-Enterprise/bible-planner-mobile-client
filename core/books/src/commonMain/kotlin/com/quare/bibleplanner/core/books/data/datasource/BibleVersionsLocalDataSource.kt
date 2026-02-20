package com.quare.bibleplanner.core.books.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.data.dto.VersionDto
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

internal class BibleVersionsLocalDataSource(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {
    suspend fun getCachedVersions(): List<VersionDto>? {
        val preferences = dataStore.data.first()
        val cachedData = preferences[stringPreferencesKey(CACHE_KEY)] ?: return null
        return runCatching {
            json.decodeFromString<List<VersionDto>>(cachedData)
        }.getOrNull()
    }

    suspend fun getCacheTimestamp(): Long? {
        val preferences = dataStore.data.first()
        return preferences[longPreferencesKey(TIMESTAMP_KEY)]
    }

    suspend fun saveToCache(
        versions: List<VersionDto>,
        timestamp: Long,
    ) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(CACHE_KEY)] = json.encodeToString(versions)
            preferences[longPreferencesKey(TIMESTAMP_KEY)] = timestamp
        }
    }

    companion object {
        private const val CACHE_KEY = "bible_versions_cache"
        private const val TIMESTAMP_KEY = "bible_versions_timestamp"
    }
}
