package com.quare.bibleplanner.core.chapterlistening.domain.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class ListeningReaderAttachmentsTest {
    private val detachedStopDelay = 2.seconds

    private lateinit var readerAttachments: ListeningReaderAttachments
    private var stopCount = 0

    @Test
    fun `GIVEN the only reader WHEN it detaches and the grace passes THEN stops once`() = runTest {
        // Given
        prepareScenario(attachedReaders = 1)

        // When
        readerAttachments.detach(::stop)
        advanceTimeBy(detachedStopDelay * 3)
        runCurrent()

        // Then
        assertEquals(1, stopCount)
    }

    @Test
    fun `GIVEN the only reader detached WHEN the grace has not passed yet THEN does not stop`() = runTest {
        // Given
        prepareScenario(attachedReaders = 1)
        readerAttachments.detach(::stop)

        // When
        advanceTimeBy(detachedStopDelay - 1.milliseconds)
        runCurrent()

        // Then
        assertEquals(0, stopCount)
    }

    @Test
    fun `GIVEN the only reader detached WHEN another reader attaches within the grace THEN does not stop`() = runTest {
        // Given
        prepareScenario(attachedReaders = 1)
        readerAttachments.detach(::stop)
        advanceTimeBy(detachedStopDelay / 2)

        // When
        readerAttachments.attach()
        advanceTimeBy(detachedStopDelay * 2)
        runCurrent()

        // Then
        assertEquals(0, stopCount)
    }

    @Test
    fun `GIVEN two readers WHEN one detaches THEN does not stop`() = runTest {
        // Given
        prepareScenario(attachedReaders = 2)

        // When
        readerAttachments.detach(::stop)
        advanceTimeBy(detachedStopDelay * 2)
        runCurrent()

        // Then
        assertEquals(0, stopCount)
    }

    @Test
    fun `GIVEN no reader attached WHEN one detaches THEN stops after the grace`() = runTest {
        // Given
        prepareScenario(attachedReaders = 0)

        // When
        readerAttachments.detach(::stop)
        advanceTimeBy(detachedStopDelay * 2)
        runCurrent()

        // Then
        assertEquals(1, stopCount)
    }

    @Test
    fun `GIVEN the only reader detached WHEN cancelling the pending stop THEN does not stop`() = runTest {
        // Given
        prepareScenario(attachedReaders = 1)
        readerAttachments.detach(::stop)

        // When
        readerAttachments.cancelPendingStop()
        advanceTimeBy(detachedStopDelay * 2)
        runCurrent()

        // Then
        assertEquals(0, stopCount)
    }

    @Test
    fun `GIVEN a reader replaced within the grace WHEN the new one detaches too THEN stops once after its own grace`() =
        runTest {
            // Given
            prepareScenario(attachedReaders = 1)
            readerAttachments.detach(::stop)
            advanceTimeBy(detachedStopDelay / 2)
            readerAttachments.attach()

            // When
            readerAttachments.detach(::stop)
            advanceTimeBy(detachedStopDelay * 3)
            runCurrent()

            // Then
            assertEquals(1, stopCount)
        }

    private fun stop() {
        stopCount++
    }

    private fun TestScope.prepareScenario(attachedReaders: Int) {
        readerAttachments = ListeningReaderAttachments(backgroundScope)
        repeat(attachedReaders) { readerAttachments.attach() }
    }
}
