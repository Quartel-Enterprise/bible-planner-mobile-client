package com.quare.bibleplanner.core.sync.data

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.sync.domain.FetchedSnapshot
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SnapshotPullerTest {
    private val trackedEvents = mutableListOf<String>()
    private val appliedNames = mutableListOf<String>()

    @Test
    fun `GIVEN all pulls succeed WHEN pulling THEN tracks a single sync_completed`() = runTest {
        // Given
        val puller = prepareScenario(
            FakeSynchronizer(name = "first"),
            FakeSynchronizer(name = "second"),
        )

        // When
        puller.pullAll()

        // Then
        assertEquals(listOf(AnalyticsEventNames.SYNC_COMPLETED), trackedEvents)
    }

    @Test
    fun `GIVEN one fetch fails WHEN pulling THEN tracks a single sync_failed`() = runTest {
        // Given
        val puller = prepareScenario(
            FakeSynchronizer(name = "first"),
            FakeSynchronizer(
                name = "second",
                shouldFailFetch = true,
            ),
        )

        // When
        puller.pullAll()

        // Then
        assertEquals(listOf(AnalyticsEventNames.SYNC_FAILED), trackedEvents)
    }

    @Test
    fun `GIVEN one apply fails WHEN pulling THEN tracks a single sync_failed`() = runTest {
        // Given
        val puller = prepareScenario(
            FakeSynchronizer(
                name = "first",
                shouldFailApply = true,
            ),
            FakeSynchronizer(name = "second"),
        )

        // When
        puller.pullAll()

        // Then
        assertEquals(listOf(AnalyticsEventNames.SYNC_FAILED), trackedEvents)
    }

    @Test
    fun `GIVEN every fetch fails WHEN pulling THEN tracks a single sync_failed`() = runTest {
        // Given
        val puller = prepareScenario(
            FakeSynchronizer(
                name = "first",
                shouldFailFetch = true,
            ),
            FakeSynchronizer(
                name = "second",
                shouldFailFetch = true,
            ),
        )

        // When
        puller.pullAll()

        // Then
        assertEquals(listOf(AnalyticsEventNames.SYNC_FAILED), trackedEvents)
    }

    @Test
    fun `GIVEN an early fetch failure WHEN pulling THEN the remaining snapshots are still applied`() = runTest {
        // Given
        val puller = prepareScenario(
            FakeSynchronizer(
                name = "failing",
                shouldFailFetch = true,
            ),
            FakeSynchronizer(name = "succeeding"),
        )

        // When
        puller.pullAll()

        // Then
        assertEquals(listOf("succeeding"), appliedNames)
    }

    @Test
    fun `GIVEN an early apply failure WHEN pulling THEN the remaining snapshots are still applied`() = runTest {
        // Given
        val puller = prepareScenario(
            FakeSynchronizer(
                name = "failing",
                shouldFailApply = true,
            ),
            FakeSynchronizer(name = "succeeding"),
        )

        // When
        puller.pullAll()

        // Then
        assertEquals(listOf("succeeding"), appliedNames)
    }

    @Test
    fun `GIVEN slow fetches WHEN pulling THEN they run concurrently`() = runTest {
        // Given
        val puller = prepareScenario(
            FakeSynchronizer(
                name = "first",
                fetchDelayMillis = FETCH_DELAY_MILLIS,
            ),
            FakeSynchronizer(
                name = "second",
                fetchDelayMillis = FETCH_DELAY_MILLIS,
            ),
            FakeSynchronizer(
                name = "third",
                fetchDelayMillis = FETCH_DELAY_MILLIS,
            ),
        )

        // When
        puller.pullAll()

        // Then
        assertEquals(FETCH_DELAY_MILLIS, currentTime)
        assertEquals(listOf("first", "second", "third"), appliedNames)
    }

    @Test
    fun `GIVEN fetches finishing out of order WHEN pulling THEN snapshots are applied in registration order`() =
        runTest {
            // Given
            val puller = prepareScenario(
                FakeSynchronizer(
                    name = "chapters",
                    fetchDelayMillis = FETCH_DELAY_MILLIS * 2,
                ),
                FakeSynchronizer(
                    name = "verses",
                    fetchDelayMillis = FETCH_DELAY_MILLIS,
                ),
            )

            // When
            puller.pullAll()

            // Then
            assertEquals(listOf("chapters", "verses"), appliedNames)
        }

    @Test
    fun `GIVEN a pull WHEN applying THEN every fetch completes before the first apply`() = runTest {
        // Given
        val events = mutableListOf<String>()
        val puller = prepareScenario(
            FakeSynchronizer(
                name = "first",
                events = events,
            ),
            FakeSynchronizer(
                name = "second",
                fetchDelayMillis = FETCH_DELAY_MILLIS,
                events = events,
            ),
        )

        // When
        puller.pullAll()

        // Then
        assertTrue(events.indexOf("fetched:second") < events.indexOf("applied:first"))
    }

    private fun prepareScenario(vararg synchronizers: FakeSynchronizer): SnapshotPuller {
        synchronizers.forEach { it.appliedNames = appliedNames }
        return SnapshotPuller(
            synchronizers = synchronizers.toList(),
            trackEvent = { name, _ -> trackedEvents += name },
        )
    }

    private companion object {
        const val FETCH_DELAY_MILLIS = 1_000L
    }
}

private class FakeSynchronizer(
    private val name: String,
    private val shouldFailFetch: Boolean = false,
    private val shouldFailApply: Boolean = false,
    private val fetchDelayMillis: Long = 0L,
    private val events: MutableList<String> = mutableListOf(),
) : Synchronizer {
    var appliedNames: MutableList<String> = mutableListOf()

    override suspend fun seed(now: Long) = Unit

    override suspend fun runPushLoop() = Unit

    override suspend fun pushPendingOnce() = Unit

    override suspend fun observeRealtime() = Unit

    override suspend fun fetchSnapshot(): FetchedSnapshot {
        delay(fetchDelayMillis)
        if (shouldFailFetch) {
            error("fetch failed")
        }
        events += "fetched:$name"
        return FetchedSnapshot {
            if (shouldFailApply) {
                error("apply failed")
            }
            events += "applied:$name"
            appliedNames += name
        }
    }

    override suspend fun clearLocal() = Unit
}
