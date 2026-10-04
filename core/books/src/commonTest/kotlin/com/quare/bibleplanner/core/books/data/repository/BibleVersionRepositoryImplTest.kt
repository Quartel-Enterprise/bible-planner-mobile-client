package com.quare.bibleplanner.core.books.data.repository

import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsLocalDataSource
import com.quare.bibleplanner.core.books.data.datasource.BibleVersionsRemoteDataSource
import com.quare.bibleplanner.core.books.data.dto.VersionDto
import com.quare.bibleplanner.core.books.data.mapper.VersionMapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
internal class BibleVersionRepositoryImplTest {
    private val freshCacheAge: Duration = 5.minutes
    private val staleCacheAge: Duration = 20.minutes
    private val clockRegression: Duration = (-26).hours
    private lateinit var repository: BibleVersionRepositoryImpl
    private lateinit var remoteDataSource: FakeBibleVersionsRemoteDataSource
    private lateinit var localDataSource: FakeBibleVersionsLocalDataSource

    @Test
    fun `GIVEN a fresh cache WHEN getting the versions THEN serves the cache without hitting remote`() = runTest {
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
    fun `GIVEN a fresh cache WHEN forcing the refresh THEN bypasses the cache`() = runTest {
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
    fun `GIVEN an expired cache WHEN getting the versions THEN refreshes the content version`() = runTest {
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
    fun `GIVEN a stored timestamp in the future WHEN getting the versions THEN refreshes`() = runTest {
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
    fun `GIVEN a fresh cache WHEN forcing the refresh THEN stores the fetched versions with the current timestamp`() =
        runTest {
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
    fun `GIVEN a failing remote WHEN forcing the refresh THEN falls back to the cache`() = runTest {
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
    fun `GIVEN a fresh cache WHEN observing the versions THEN emits the cache without hitting remote`() = runTest {
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
    fun `GIVEN observed versions WHEN the cache is updated THEN re-emits the versions`() = runTest {
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
    fun `GIVEN a stale cache WHEN observing the versions THEN emits the refreshed versions`() = runTest {
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
            expected = listOf(listOf(CACHED_CONTENT_VERSION), listOf(REMOTE_CONTENT_VERSION)),
            actual = emissions,
        )
    }

    @Test
    fun `GIVEN a stale cache and a pending refresh WHEN observing THEN emits the cached versions first`() = runTest {
        // Given
        val remoteGate = CompletableDeferred<Unit>()
        prepareScenario(
            cacheAge = staleCacheAge,
            remoteGate = remoteGate,
        )

        // When
        val emissions = mutableListOf<List<String>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeVersions().collect { versions ->
                emissions.add(versions.map { it.version })
            }
        }

        // Then
        assertEquals(
            expected = listOf(listOf(CACHED_CONTENT_VERSION)),
            actual = emissions,
        )
    }

    @Test
    fun `GIVEN observed stale versions WHEN the refresh answers THEN emits the refreshed versions`() = runTest {
        // Given
        val remoteGate = CompletableDeferred<Unit>()
        prepareScenario(
            cacheAge = staleCacheAge,
            remoteGate = remoteGate,
        )
        val emissions = mutableListOf<List<String>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeVersions().collect { versions ->
                emissions.add(versions.map { it.version })
            }
        }

        // When
        remoteGate.complete(Unit)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(listOf(CACHED_CONTENT_VERSION), listOf(REMOTE_CONTENT_VERSION)),
            actual = emissions,
        )
    }

    private fun prepareScenario(
        cacheAge: Duration,
        remoteResult: Result<List<VersionDto>> = Result.success(listOf(versionDto(REMOTE_CONTENT_VERSION))),
        remoteGate: CompletableDeferred<Unit>? = null,
    ) {
        remoteDataSource = FakeBibleVersionsRemoteDataSource(
            result = remoteResult,
            gate = remoteGate,
        )
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

    private companion object {
        const val NOW = 1_000_000_000L
        const val CACHED_CONTENT_VERSION = "1.2.0"
        const val REMOTE_CONTENT_VERSION = "1.3.0"
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
    private val gate: CompletableDeferred<Unit>?,
) : BibleVersionsRemoteDataSource {
    var callCount = 0
        private set

    override suspend fun getVersions(): Result<List<VersionDto>> {
        callCount++
        gate?.await()
        return result
    }
}

private class FakeBibleVersionsLocalDataSource(
    private val cacheTimestamp: Long?,
    cachedVersions: List<VersionDto>?,
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
