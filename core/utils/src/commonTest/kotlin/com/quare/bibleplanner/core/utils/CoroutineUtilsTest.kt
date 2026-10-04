package com.quare.bibleplanner.core.utils

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class CoroutineUtilsTest {
    @Test
    fun `GIVEN a block that returns WHEN running it catching THEN wraps the value in a success`() {
        // Given
        val value = 42

        // When
        val result = suspendRunCatching { value }

        // Then
        assertEquals(Result.success(42), result)
    }

    @Test
    fun `GIVEN a block that throws WHEN running it catching THEN wraps the exception in a failure`() {
        // Given
        val message = "boom"

        // When
        val result = suspendRunCatching { error(message) }

        // Then
        val exception = result.exceptionOrNull()
        assertIs<IllegalStateException>(exception)
        assertEquals("boom", exception.message)
    }

    @Test
    fun `GIVEN a cancelled block WHEN running it catching THEN rethrows the cancellation`() {
        // Given
        val cancellation = CancellationException("cancelled")

        // When
        val result = runCatching {
            suspendRunCatching { throw cancellation }
        }

        // Then
        assertIs<CancellationException>(result.exceptionOrNull())
    }
}
