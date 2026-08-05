package com.quare.bibleplanner.core.books.data.repository

import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsLocalDataSource
import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsRemoteDataSource
import com.quare.bibleplanner.core.books.data.dto.VersionDto
import com.quare.bibleplanner.core.books.data.mapper.VersionMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private const val NOW = 1_000_000_000L
private const val CACHED_CONTENT_VERSION = "1.2.0"
private const val REMOTE_CONTENT_VERSION = "1.3.0"

@OptIn(ExperimentalCoroutinesApi::class)
internal class BibleVersionRepositoryImplTest {
    private val freshCacheAge: Duration = 5.minutes
    private val staleCacheAge: Duration = 20.minutes
    private val clockRegression: Duration = (-26).hours
    private lateinit var repository: BibleVersionRepositoryImpl
    private lateinit var remoteDataSource: FakeBibleVersionsRemoteDataSource
    private lateinit var localDataSource: FakeBibleVersionsLocalDataSource

    @Test
    fun `should serve the cache without hitting remote when the cache is fresh`() = runTest {
        // Given
        prepareScenario(cacheAge = freshCacheAge)

        // When
        val versions = repository.getVersions(forceRefresh = false)

        // Then
        assertEquals(expected = 0, actual = remoteDataSource.callCount)
        assertEquals(
            expected = listOf(CACHED_CONTENT_VERSION),
            actual = versions.getOrThrow().map { it.version },
        )
    }

    @Test
    fun `should bypass a fresh cache when the refresh is forced`() = runTest {
        // Given
        prepareScenario(cacheAge = freshCacheAge)

        // When
        val versions = repository.getVersions(forceRefresh = true)

        // Then
        assertEquals(expected = 1, actual = remoteDataSource.callCount)
        assertEquals(
            expected = listOf(REMOTE_CONTENT_VERSION),
            actual = versions.getOrThrow().map { it.version },
        )
    }

    @Test
    fun `should refresh the content version stamped after the cache expires`() = runTest {
        // Given
        prepareScenario(cacheAge = staleCacheAge)

        // When
        val versions = repository.getVersions(forceRefresh = false)

        // Then
        assertEquals(expected = 1, actual = remoteDataSource.callCount)
        assertEquals(
            expected = listOf(REMOTE_CONTENT_VERSION),
            actual = versions.getOrThrow().map { it.version },
        )
    }

    @Test
    fun `should refresh when the stored timestamp is in the future`() = runTest {
        // Given
        prepareScenario(cacheAge = clockRegression)

        // When
        val versions = repository.getVersions(forceRefresh = false)

        // Then
        assertEquals(expected = 1, actual = remoteDataSource.callCount)
        assertEquals(
            expected = listOf(REMOTE_CONTENT_VERSION),
            actual = versions.getOrThrow().map { it.version },
        )
    }

    @Test
    fun `should store the fetched versions stamped with the current timestamp`() = runTest {
        // Given
        prepareScenario(cacheAge = freshCacheAge)

        // When
        repository.getVersions(forceRefresh = true)

        // Then
        assertEquals(expected = NOW, actual = localDataSource.savedTimestamp)
        assertEquals(
            expected = listOf(REMOTE_CONTENT_VERSION),
            actual = localDataSource.savedVersions?.map { it.version },
        )
    }

    @Test
    fun `should fall back to the cache when the forced refresh fails`() = runTest {
        // Given
        prepareScenario(
            cacheAge = staleCacheAge,
            remoteResult = Result.failure(IllegalStateException("offline")),
        )

        // When
        val versions = repository.getVersions(forceRefresh = true)

        // Then
        assertEquals(
            expected = listOf(CACHED_CONTENT_VERSION),
            actual = versions.getOrThrow().map { it.version },
        )
    }

    @Test
    fun `should observe the cached versions without hitting remote when the cache is fresh`() = runTest {
        // Given
        prepareScenario(cacheAge = freshCacheAge)

        // When
        val emissions = mutableListOf<List<String>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeVersions().collect { versions ->
                emissions.add(versions.map { it.version })
            }
        }

        // Then
        assertEquals(expected = 0, actual = remoteDataSource.callCount)
        assertEquals(
            expected = listOf(listOf(CACHED_CONTENT_VERSION)),
            actual = emissions,
        )
    }

    @Test
    fun `should re-emit the observed versions when the cache is updated`() = runTest {
        // Given
        prepareScenario(cacheAge = freshCacheAge)
        val emissions = mutableListOf<List<String>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeVersions().collect { versions ->
                emissions.add(versions.map { it.version })
            }
        }

        // When
        localDataSource.updateCache(listOf(versionDto(REMOTE_CONTENT_VERSION)))

        // Then
        assertEquals(
            expected = listOf(
                listOf(CACHED_CONTENT_VERSION),
                listOf(REMOTE_CONTENT_VERSION),
            ),
            actual = emissions,
        )
    }

    @Test
    fun `should observe the refreshed versions when the cache is stale`() = runTest {
        // Given
        prepareScenario(cacheAge = staleCacheAge)

        // When
        val emissions = mutableListOf<List<String>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeVersions().collect { versions ->
                emissions.add(versions.map { it.version })
            }
        }

        // Then
        assertEquals(expected = 1, actual = remoteDataSource.callCount)
        assertEquals(
            expected = listOf(listOf(REMOTE_CONTENT_VERSION)),
            actual = emissions,
        )
    }

    private fun prepareScenario(
        cacheAge: Duration,
        remoteResult: Result<List<VersionDto>> = Result.success(listOf(versionDto(REMOTE_CONTENT_VERSION))),
    ) {
        remoteDataSource = FakeBibleVersionsRemoteDataSource(result = remoteResult)
        localDataSource = FakeBibleVersionsLocalDataSource(
            cachedVersions = listOf(versionDto(CACHED_CONTENT_VERSION)),
            cacheTimestamp = NOW - cacheAge.inWholeMilliseconds,
        )
        repository = BibleVersionRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            versionMapper = VersionMapper(),
            currentTimestampProvider = { NOW },
        )
    }
}

private fun versionDto(version: String): VersionDto = VersionDto(
    id = "ACF",
    name = "Almeida Corrigida Fiel",
    version = version,
    language = "pt",
    country = "br",
    chapters = 1189,
    size = 8_245_560L,
)

private class FakeBibleVersionsRemoteDataSource(
    private val result: Result<List<VersionDto>>,
) : BibleVersionsRemoteDataSource {
    var callCount = 0
        private set

    override suspend fun getVersions(): Result<List<VersionDto>> {
        callCount++
        return result
    }
}

private class FakeBibleVersionsLocalDataSource(
    cachedVersions: List<VersionDto>?,
    private val cacheTimestamp: Long?,
) : BibleVersionsLocalDataSource {
    private val cachedVersionsFlow = MutableStateFlow(cachedVersions)
    var savedVersions: List<VersionDto>? = null
        private set
    var savedTimestamp: Long? = null
        private set

    fun updateCache(versions: List<VersionDto>) {
        cachedVersionsFlow.value = versions
    }

    override suspend fun getCachedVersions(): List<VersionDto>? = cachedVersionsFlow.value

    override fun observeCachedVersions(): Flow<List<VersionDto>?> = cachedVersionsFlow

    override suspend fun getCacheTimestamp(): Long? = cacheTimestamp

    override suspend fun saveToCache(
        versions: List<VersionDto>,
        timestamp: Long,
    ) {
        savedVersions = versions
        savedTimestamp = timestamp
        cachedVersionsFlow.value = versions
    }
}
