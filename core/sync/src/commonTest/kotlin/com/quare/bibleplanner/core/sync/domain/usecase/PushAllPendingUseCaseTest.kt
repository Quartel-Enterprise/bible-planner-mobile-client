package com.quare.bibleplanner.core.sync.domain.usecase

import com.quare.bibleplanner.core.sync.domain.Synchronizer
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class PushAllPendingUseCaseTest {
    @Test
    fun `GIVEN several datasets WHEN pushing all pending THEN flushes every one of them`() = runTest {
        // Given
        val first = FlushingSynchronizer(shouldFail = false)
        val second = FlushingSynchronizer(shouldFail = false)
        val useCase = PushAllPendingUseCase(listOf(first, second))

        // When
        useCase()

        // Then
        assertTrue(first.hasPushed)
        assertTrue(second.hasPushed)
    }

    @Test
    fun `GIVEN a dataset that fails to push WHEN pushing all pending THEN propagates the failure`() = runTest {
        // Given
        val useCase = PushAllPendingUseCase(
            listOf(
                FlushingSynchronizer(shouldFail = false),
                FlushingSynchronizer(shouldFail = true),
            ),
        )

        // When
        val error = assertFailsWith<IllegalStateException> { useCase() }

        // Then
        assertEquals(
            expected = "push failed",
            actual = error.message,
        )
    }
}

private class FlushingSynchronizer(
    private val shouldFail: Boolean,
) : Synchronizer {
    var hasPushed = false

    override suspend fun seed(now: Long) = error("unused")

    override suspend fun runPushLoop() = error("unused")

    override suspend fun pushPendingOnce() {
        if (shouldFail) error("push failed")
        hasPushed = true
    }

    override suspend fun observeRealtime() = error("unused")

    override suspend fun pullSnapshot() = error("unused")

    override suspend fun clearLocal() = error("unused")
}
