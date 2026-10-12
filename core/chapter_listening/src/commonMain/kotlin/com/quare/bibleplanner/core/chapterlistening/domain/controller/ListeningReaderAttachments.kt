package com.quare.bibleplanner.core.chapterlistening.domain.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ListeningReaderAttachments(
    private val scope: CoroutineScope,
) {
    private val detachedStopDelay: Duration = 2.seconds
    private var attachedReaders = 0
    private var detachedStopJob: Job? = null

    fun attach() {
        attachedReaders++
        detachedStopJob?.cancel()
    }

    /*
     * Why: replacing the top reader with the next chapter detaches the old one only after the new
     * one attached, but the grace still covers a replace that lands the other way round.
     */
    fun detach(onAllDetached: () -> Unit) {
        attachedReaders = (attachedReaders - 1).coerceAtLeast(0)
        if (attachedReaders > 0) return
        detachedStopJob?.cancel()
        detachedStopJob = scope.launch {
            delay(detachedStopDelay)
            if (attachedReaders == 0) onAllDetached()
        }
    }

    fun cancelPendingStop() {
        detachedStopJob?.cancel()
    }
}
