package com.quare.bibleplanner.core.presentation.bridge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
internal class FlowWatcherTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val received = mutableListOf<Int>()
    private lateinit var watcher: FlowWatcher<Int>
    private lateinit var watch: Watch
    private val ownerScope = CoroutineScope(SupervisorJob())

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        if (::watch.isInitialized) watch.cancel()
        ownerScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a flow of several values WHEN observing THEN calls onEach for every value in order`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(flow = flowOf(1, 2, 3))

            // When
            watch = watcher.observe { received += it }

            // Then
            assertEquals(
                expected = listOf(1, 2, 3),
                actual = received,
            )
        }

    @Test
    fun `GIVEN an observed state flow WHEN its value changes THEN calls onEach with the current and the new value`() =
        runTest(testDispatcher) {
            // Given
            val state = MutableStateFlow(1)
            prepareScenario(flow = state)
            watch = watcher.observe { received += it }

            // When
            state.value = 2

            // Then
            assertEquals(
                expected = listOf(1, 2),
                actual = received,
            )
        }

    @Test
    fun `GIVEN a flow WHEN observing THEN collects it on the immediate main dispatcher`() = runTest(testDispatcher) {
        // Given
        var collectingInterceptor: ContinuationInterceptor? = null
        prepareScenario(
            flow = flow {
                collectingInterceptor = currentCoroutineContext()[ContinuationInterceptor]
                emit(1)
            },
        )

        // When
        watch = watcher.observe { received += it }

        // Then
        assertSame(
            expected = Dispatchers.Main.immediate,
            actual = collectingInterceptor,
        )
    }

    @Test
    fun `GIVEN a cancelled watch WHEN the flow emits THEN does not call onEach`() = runTest(testDispatcher) {
        // Given
        val values = MutableSharedFlow<Int>()
        prepareScenario(flow = values)
        watch = watcher.observe { received += it }
        values.emit(1)
        watch.cancel()

        // When
        values.emit(2)

        // Then
        assertEquals(
            expected = listOf(1),
            actual = received,
        )
    }

    @Test
    fun `GIVEN an observed flow WHEN cancelling the watch twice THEN does not fail`() = runTest(testDispatcher) {
        // Given
        prepareScenario(flow = MutableSharedFlow())
        watch = watcher.observe { received += it }

        // When
        watch.cancel()
        watch.cancel()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = received,
        )
    }

    @Test
    fun `GIVEN a main dispatcher that queues work WHEN the flow emits right after observing THEN onEach receives it`() =
        runTest {
            // Given
            val queuingDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(queuingDispatcher)
            val values = MutableSharedFlow<Int>(extraBufferCapacity = 1)
            prepareScenario(flow = values)
            watch = watcher.observe { received += it }

            // When
            values.tryEmit(1)
            advanceUntilIdle()

            // Then
            assertEquals(
                expected = listOf(1),
                actual = received,
            )
        }

    @Test
    fun `GIVEN a flow that throws WHEN observing THEN the exception does not escape observe`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                flow = flow {
                    emit(1)
                    error("upstream failure")
                },
            )

            // When
            watch = watcher.observe { received += it }

            // Then
            assertEquals(
                expected = listOf(1),
                actual = received,
            )
        }

    @Test
    fun `GIVEN two observers and one cancelled WHEN the flow emits THEN only the active one receives it`() =
        runTest(testDispatcher) {
            // Given
            val values = MutableSharedFlow<Int>()
            prepareScenario(flow = values)
            val cancelledReceived = mutableListOf<Int>()
            watch = watcher.observe { received += it }
            watcher.observe { cancelledReceived += it }.cancel()

            // When
            values.emit(1)

            // Then
            assertEquals(
                expected = listOf(1),
                actual = received,
            )
            assertEquals(
                expected = emptyList(),
                actual = cancelledReceived,
            )
        }

    @Test
    fun `GIVEN an observed flow WHEN the owner scope is cancelled THEN does not call onEach anymore`() =
        runTest(testDispatcher) {
            // Given
            val values = MutableSharedFlow<Int>()
            prepareScenario(flow = values)
            watch = watcher.observe { received += it }
            values.emit(1)

            // When
            ownerScope.cancel()
            values.emit(2)

            // Then
            assertEquals(
                expected = listOf(1),
                actual = received,
            )
        }

    private fun prepareScenario(flow: Flow<Int>) {
        watcher = FlowWatcher(
            flow = flow,
            scope = ownerScope,
        )
    }
}
