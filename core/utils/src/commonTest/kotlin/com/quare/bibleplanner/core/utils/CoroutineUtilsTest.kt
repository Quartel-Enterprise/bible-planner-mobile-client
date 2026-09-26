package com.quare.bibleplanner.core.utils

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

internal class CoroutineUtilsTest {
    @Test
    fun `GIVEN a block that returns WHEN running it catching THEN wraps the value in a success`() {
        // When
        val result = suspendRunCatching { 42 }

        // Then
        assertEquals(Result.success(42), result)
    }

    @Test
    fun `GIVEN a block that throws WHEN running it catching THEN wraps the exception in a failure`() {
        // When
        val result = suspendRunCatching { error("boom") }

        // Then
        val exception = result.exceptionOrNull()
        assertIs<IllegalStateException>(exception)
        assertEquals("boom", exception.message)
    }

    @Test
    fun `GIVEN a cancelled block WHEN running it catching THEN rethrows the cancellation`() {
        // When / Then
        assertFailsWith<CancellationException> {
            suspendRunCatching { throw CancellationException("cancelled") }
        }
    }
}
