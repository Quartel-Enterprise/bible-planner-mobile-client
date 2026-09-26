package com.quare.bibleplanner.core.provider.connectivity

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

internal class DesktopNetworkConnectivityObserverTest {
    private val pollInterval = 5.seconds
    private lateinit var observer: DesktopNetworkConnectivityObserver

    @Test
    fun `GIVEN a stable network WHEN polling several times THEN emits the state only once`() = runTest {
        // Given
        val states = mutableListOf<Boolean>()
        backgroundScope.launch { observer.observe().collect(states::add) }

        // When
        advanceTimeBy(pollInterval * 3)

        // Then
        assertEquals(1, states.size)
    }

    @BeforeTest
    fun setUp() {
        observer = DesktopNetworkConnectivityObserver(pollInterval)
    }
}
