package com.quare.bibleplanner.core.sync.data

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SnapshotPullerTest {
    private val trackedEvents = mutableListOf<String>()

    @Test
    fun `GIVEN all pulls succeed WHEN pulling THEN tracks a single sync_completed`() = runTest {
        // Given
        val puller = prepareScenario(
            FakeSynchronizer(shouldFailPull = false),
            FakeSynchronizer(shouldFailPull = false),
        )

        // When
        puller.pullAll()

        // Then
        assertEquals(listOf(AnalyticsEventNames.SYNC_COMPLETED), trackedEvents)
    }

    @Test
    fun `GIVEN one pull fails WHEN pulling THEN tracks a single sync_failed`() = runTest {
        // Given
        val puller = prepareScenario(
            FakeSynchronizer(shouldFailPull = false),
            FakeSynchronizer(shouldFailPull = true),
        )

        // When
        puller.pullAll()

        // Then
        assertEquals(listOf(AnalyticsEventNames.SYNC_FAILED), trackedEvents)
    }

    @Test
    fun `GIVEN every pull fails WHEN pulling THEN tracks a single sync_failed`() = runTest {
        // Given
        val puller = prepareScenario(
            FakeSynchronizer(shouldFailPull = true),
            FakeSynchronizer(shouldFailPull = true),
        )

        // When
        puller.pullAll()

        // Then
        assertEquals(listOf(AnalyticsEventNames.SYNC_FAILED), trackedEvents)
    }

    @Test
    fun `GIVEN an early pull failure WHEN pulling THEN the remaining synchronizers still pull`() = runTest {
        // Given
        val failing = FakeSynchronizer(shouldFailPull = true)
        val succeeding = FakeSynchronizer(shouldFailPull = false)
        val puller = prepareScenario(failing, succeeding)

        // When
        puller.pullAll()

        // Then
        assertTrue(succeeding.pulled)
    }

    private fun prepareScenario(vararg synchronizers: FakeSynchronizer): SnapshotPuller = SnapshotPuller(
        synchronizers = synchronizers.toList(),
        trackEvent = { name, _ -> trackedEvents += name },
    )
}

private class FakeSynchronizer(
    private val shouldFailPull: Boolean,
) : Synchronizer {
    var pulled = false

    override suspend fun seed(now: Long) = Unit

    override suspend fun runPushLoop() = Unit

    override suspend fun pushPendingOnce() = Unit

    override suspend fun observeRealtime() = Unit

    override suspend fun pullSnapshot() {
        pulled = true
        if (shouldFailPull) {
            error("pull failed")
        }
    }

    override suspend fun clearLocal() = Unit
}
