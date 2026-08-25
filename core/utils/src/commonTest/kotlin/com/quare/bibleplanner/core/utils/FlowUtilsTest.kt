package com.quare.bibleplanner.core.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
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
    fun `emits the first value without waiting for the window`() = runTest {
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
    fun `drops every value but the latest one produced inside the window`() = runTest {
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
    fun `keeps every value when they are further apart than the window`() = runTest {
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
}
