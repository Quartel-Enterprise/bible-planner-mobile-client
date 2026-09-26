package com.quare.bibleplanner.core.books.data.datasource

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.books.data.dto.VersionDto
import com.quare.bibleplanner.core.books.data.mapper.CachedVersionsJsonMapper
import com.quare.bibleplanner.core.books.fake.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BibleVersionsLocalDataSourceImplTest {
    private val version = VersionDto(
        id = "WEB",
        name = "World English Bible",
        version = "1.0.0",
        language = "en",
        country = "us",
        chapters = 1189,
        size = 4_000_000L,
    )

    private lateinit var dataSource: BibleVersionsLocalDataSourceImpl

    @Test
    fun `GIVEN an empty cache WHEN reading it THEN has no versions nor timestamp`() = runTest {
        // Given
        prepareScenario()

        // When
        val versions = dataSource.getCachedVersions()
        val observed = dataSource.observeCachedVersions().first()
        val timestamp = dataSource.getCacheTimestamp()

        // Then
        assertNull(versions)
        assertNull(observed)
        assertNull(timestamp)
    }

    @Test
    fun `GIVEN saved versions WHEN reading the cache THEN returns them with their timestamp`() = runTest {
        // Given
        prepareScenario()
        dataSource.saveToCache(
            versions = listOf(version),
            timestamp = 42L,
        )

        // When
        val versions = dataSource.getCachedVersions()
        val observed = dataSource.observeCachedVersions().first()
        val timestamp = dataSource.getCacheTimestamp()

        // Then
        assertEquals(listOf(version), versions)
        assertEquals(listOf(version), observed)
        assertEquals(42L, timestamp)
    }

    @Test
    fun `GIVEN a corrupted cache WHEN reading it THEN treats it as missing`() = runTest {
        // Given
        prepareScenario(cachedJson = "not json")

        // When
        val versions = dataSource.getCachedVersions()
        val observed = dataSource.observeCachedVersions().first()

        // Then
        assertNull(versions)
        assertNull(observed)
    }

    private fun prepareScenario(cachedJson: String? = null) {
        val json = Json { ignoreUnknownKeys = true }
        val initial = cachedJson?.let { mutablePreferencesOf(stringPreferencesKey("bible_versions_cache") to it) }
        dataSource = BibleVersionsLocalDataSourceImpl(
            dataStore = initial?.let(::InMemoryPreferencesDataStore) ?: InMemoryPreferencesDataStore(),
            json = json,
            cachedVersionsJsonMapper = CachedVersionsJsonMapper(json),
        )
    }
}
