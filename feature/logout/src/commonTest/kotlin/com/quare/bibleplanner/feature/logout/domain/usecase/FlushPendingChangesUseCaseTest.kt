package com.quare.bibleplanner.feature.logout.domain.usecase

import com.quare.bibleplanner.core.sync.domain.usecase.PushAllPending
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

internal class FlushPendingChangesUseCaseTest {
    private val flushTimeout = 5.seconds
    private lateinit var useCase: FlushPendingChangesUseCase

    @Test
    fun `GIVEN a push that completes within the timeout WHEN flushing THEN returns success`() = runTest {
        // Given
        prepareScenario(push = { })

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `GIVEN a push that fails WHEN flushing THEN returns the push error`() = runTest {
        // Given
        prepareScenario(push = { throw IllegalStateException("boom") })

        // When
        val thrown = useCase().exceptionOrNull()

        // Then
        // Coroutine stacktrace recovery may copy the exception, so assert type + message, not identity.
        assertIs<IllegalStateException>(thrown)
        assertEquals("boom", thrown.message)
    }

    @Test
    fun `GIVEN a push that exceeds the timeout WHEN flushing THEN returns FlushTimeoutException`() = runTest {
        // Given
        prepareScenario(push = { delay(flushTimeout * 2) })

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertIs<FlushTimeoutException>(result.exceptionOrNull())
    }

    private fun prepareScenario(push: PushAllPending) {
        useCase = FlushPendingChangesUseCase(pushAllPending = push, flushTimeout = flushTimeout)
    }
}
