package com.quare.bibleplanner.core.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class FlowUtilsTest {
    private val window = 1.seconds

    @Test
    fun `GIVEN a throttled flow WHEN the first value is emitted THEN emits it without waiting for the window`() =
        runTest {
            // Given
            val source = MutableSharedFlow<Int>()
            val emissions = mutableListOf<Int>()
            backgroundScope.launch { source.throttleLatest(window).toList(emissions) }
            runCurrent()

            // When
            source.emit(1)
            runCurrent()

            // Then
            assertEquals(expected = listOf(1), actual = emissions)
        }

    @Test
    fun `GIVEN a throttled flow WHEN several values arrive inside the window THEN keeps only the latest`() = runTest {
        // Given
        val source = MutableSharedFlow<Int>(extraBufferCapacity = 8)
        val emissions = mutableListOf<Int>()
        backgroundScope.launch { source.throttleLatest(window).toList(emissions) }
        runCurrent()
        source.emit(1)
        runCurrent()

        // When
        source.emit(2)
        source.emit(3)
        source.emit(4)
        advanceTimeBy(window)
        runCurrent()

        // Then
        assertEquals(expected = listOf(1, 4), actual = emissions)
    }

    @Test
    fun `GIVEN values further apart than the window WHEN throttling THEN keeps every value`() = runTest {
        // Given
        val source = flow {
            repeat(3) { index ->
                emit(index)
                delay(window * 2)
            }
        }
        val emissions = mutableListOf<Int>()

        // When
        source.throttleLatest(window).toList(emissions)

        // Then
        assertEquals(expected = listOf(0, 1, 2), actual = emissions)
    }

    @Test
    fun `GIVEN a state flow WHEN updating its value THEN replaces the value`() {
        // Given
        val state = MutableStateFlow(0)

        // When
        state.updateValue(5)

        // Then
        assertEquals(expected = 5, actual = state.value)
    }
}
