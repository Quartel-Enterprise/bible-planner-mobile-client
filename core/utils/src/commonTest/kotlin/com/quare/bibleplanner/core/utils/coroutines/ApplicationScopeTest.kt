package com.quare.bibleplanner.core.utils.coroutines

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ApplicationScopeTest {
    @Test
    fun `GIVEN the default scope WHEN a child fails THEN the scope keeps running other work`() = runTest {
        // Given
        val scope = ApplicationScope()
        val failures = mutableListOf<Throwable>()
        val handler = CoroutineExceptionHandler { _, throwable -> failures += throwable }

        // When
        scope.launch(handler) { error("boom") }.join()
        val result = scope.launch { }.also { it.join() }

        // Then
        assertTrue(scope.isActive)
        assertTrue(result.isCompleted)
        assertEquals(listOf("boom"), failures.map { it.message })
        scope.cancel()
    }
}
