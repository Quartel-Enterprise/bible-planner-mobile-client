package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ListeningSleepTimer(
    private val scope: CoroutineScope,
    private val store: ChapterListeningStateStore,
    private val tracker: ChapterListeningAnalyticsTracker,
) {
    private val tick: Duration = 1.seconds
    private var job: Job? = null

    // Why: the countdown only runs down while the chapter plays, and pausing does not cancel it.
    fun ensureTicking(onElapsed: suspend () -> Unit) {
        if (store.sleepTimer !is ListeningSleepTimerModel.Countdown || job?.isActive == true) return
        job = scope.launch {
            while (true) {
                delay(tick)
                val current = store.state.value
                val timer = current.sleepTimer as? ListeningSleepTimerModel.Countdown ?: break
                if (current.session?.status != ListeningStatusModel.PLAYING) continue
                val remaining = timer.remaining - tick
                if (remaining > Duration.ZERO) {
                    store.update { it.copy(sleepTimer = timer.copy(remaining = remaining)) }
                    continue
                }
                store.update { it.copy(sleepTimer = ListeningSleepTimerModel.Off) }
                onElapsed()
                break
            }
        }
    }

    // Why: a sleep timer that stops before the chapter ends means the next one would be fetched for nothing.
    fun willReachNextSegment(
        current: ListeningSessionModel,
        position: Duration,
    ): Boolean = when (val timer = store.sleepTimer) {
        ListeningSleepTimerModel.Off -> true

        ListeningSleepTimerModel.EndOfChapter -> false

        is ListeningSleepTimerModel.Countdown -> {
            val end = current.verses.lastOrNull()?.end ?: Duration.ZERO
            timer.remaining > (end - position) / store.settings.speed
                .toDouble()
        }
    }

    suspend fun end(option: String) {
        tracker.trackSleepTimerEnded(option)
        store.emit(ChapterListeningEventModel.SleepTimerEnded)
    }

    fun cancel() {
        job?.cancel()
    }
}
