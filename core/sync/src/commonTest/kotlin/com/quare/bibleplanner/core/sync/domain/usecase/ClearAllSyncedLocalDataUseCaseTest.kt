package com.quare.bibleplanner.core.sync.domain.usecase

import com.quare.bibleplanner.core.sync.domain.Synchronizer
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class ClearAllSyncedLocalDataUseCaseTest {
    @Test
    fun `GIVEN several datasets WHEN clearing the synced local data THEN wipes every one of them`() = runTest {
        // Given
        val first = ClearableSynchronizer()
        val second = ClearableSynchronizer()
        val useCase = ClearAllSyncedLocalDataUseCase(listOf(first, second))

        // When
        useCase()

        // Then
        assertTrue(first.hasCleared)
        assertTrue(second.hasCleared)
    }
}

private class ClearableSynchronizer : Synchronizer {
    var hasCleared = false

    override suspend fun seed(now: Long) = error("unused")

    override suspend fun runPushLoop() = error("unused")

    override suspend fun pushPendingOnce() = error("unused")

    override suspend fun observeRealtime() = error("unused")

    override suspend fun pullSnapshot() = error("unused")

    override suspend fun clearLocal() {
        hasCleared = true
    }
}
