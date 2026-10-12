package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.findNextWordChange
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.withPosition
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/*
 * Why: every call and every player callback runs on the scope's single (main) thread, so the plain
 * vars below need no locking; the playback id drops the callbacks of a playback that was stopped
 * before they arrived.
 */
internal class ListeningPlayback(
    private val scope: CoroutineScope,
    private val audioPlayer: ChapterAudioPlayer,
    private val mediaSession: ListeningMediaSession,
    private val store: ChapterListeningStateStore,
    private val sleepTimer: ListeningSleepTimer,
    private val prefetcher: ChapterAudioPrefetcher,
    private val tracker: ChapterListeningAnalyticsTracker,
) {
    private val positionTick: Duration = 200.milliseconds
    private val minimumWordTick: Duration = 20.milliseconds
    private var playbackId = 0
    private var startedPlaybackId: Int? = null
    private var playbackJob: Job? = null
    private var hasTrackedStart = false
    private var hasPrefetched = false
    private var onSegmentEnd: () -> Unit = {}

    var audio: ChapterAudioModel? = null
    var position: Duration = Duration.ZERO

    val isSupported: Boolean
        get() = audioPlayer.isSupported

    val events: Flow<ChapterAudioPlayerEvent> = audioPlayer.events.filter { event -> event.playbackId == playbackId }

    // Why: the controller loads the next segment and itself depends on this playback, so it registers once it exists.
    fun setSegmentEndListener(listener: () -> Unit) {
        onSegmentEnd = listener
    }

    fun playFrom(
        start: Duration,
        leadingSilence: Duration = Duration.ZERO,
    ) {
        val current = store.session ?: return
        val chapterAudio = audio ?: return
        if (current.verses.isEmpty()) return
        if (!mediaSession.requestActivation()) {
            store.updateSession { it.copy(status = ListeningStatusModel.PAUSED) }
            return
        }
        silence()
        val id = playbackId
        position = start
        showPosition(start)
        store.updateSession { it.copy(status = ListeningStatusModel.PLAYING) }
        trackStartOnce(current)
        // Why: the next chapter may be generated on the server, so it is fetched once this one actually plays.
        if (!hasPrefetched) {
            hasPrefetched = true
            prefetchNextSegment(current)
        }
        playbackJob = scope.launch {
            delay(leadingSilence)
            audioPlayer.play(
                audioPath = chapterAudio.audioPath,
                position = start,
                speed = store.settings.speed,
                playbackId = id,
            )
            startedPlaybackId = id
            follow(id)
        }
        ensureSleepTimerTicking()
    }

    // Why: an interrupted chapter that is paused for good (another app took the audio) must not resume after.
    fun pause() {
        val current = store.session ?: return
        if (!current.isPlaying && current.status != ListeningStatusModel.INTERRUPTED) return
        silence()
        store.updateSession { it.copy(status = ListeningStatusModel.PAUSED) }
    }

    /*
     * Why: the system already silenced the player, and on Android pausing it here would give the audio
     * focus up for good, so the call ending would never be reported.
     */
    fun interrupt() {
        val current = store.session ?: return
        if (!current.isPlaying) return
        stopFollowing()
        store.updateSession { it.copy(status = ListeningStatusModel.INTERRUPTED) }
        tracker.trackInterrupted(current)
    }

    fun showPausedVerse(
        verseIndex: Int,
        start: Duration,
    ) {
        position = start
        audioPlayer.seekTo(start)
        store.updateSession {
            it.copy(
                verseIndex = verseIndex,
                verseProgress = 0f,
                wordIndex = null,
                status = it.status.toPausedIfFinished(),
            )
        }
    }

    fun seekPaused(start: Duration) {
        position = start
        // Why: Android's lock screen shows the player's own position, so a paused seek moves it too.
        audioPlayer.seekTo(start)
        showPosition(start)
        store.updateSession { it.copy(status = it.status.toPausedIfFinished()) }
    }

    fun finish(finishedChapter: ChapterLocationModel) {
        silence()
        position = store.session
            ?.verses
            ?.firstOrNull()
            ?.start ?: Duration.ZERO
        store.updateSession {
            it.copy(
                verseIndex = it.verses.lastIndex.coerceAtLeast(0),
                verseProgress = 1f,
                wordIndex = null,
                status = ListeningStatusModel.FINISHED,
                finishedChapter = finishedChapter,
            )
        }
    }

    fun reachEnd(id: Int) {
        if (id != playbackId) return
        silence()
        onSegmentEnd()
    }

    fun setSpeed(speed: Float) {
        if (startedPlaybackId == playbackId) audioPlayer.setSpeed(speed)
    }

    fun ensureSleepTimerTicking() {
        sleepTimer.ensureTicking(::onSleepTimerElapsed)
    }

    fun silence() {
        stopFollowing()
        audioPlayer.pause()
    }

    fun reset() {
        silence()
        hasTrackedStart = false
        hasPrefetched = false
        audio = null
        position = Duration.ZERO
    }

    fun release() {
        silence()
        audioPlayer.release()
        audio = null
    }

    // Why: the position read here is where a later resume picks up, so it is taken before the player pauses.
    private fun stopFollowing() {
        if (startedPlaybackId == playbackId) position = audioPlayer.position
        playbackId++
        playbackJob?.cancel()
    }

    // Why: a segment that ends mid-chapter must stop exactly at its last verse, before the next one starts.
    private suspend fun follow(id: Int) {
        while (id == playbackId) {
            position = audioPlayer.position
            val end = store.session
                ?.verses
                ?.lastOrNull()
                ?.end ?: return
            if (position >= end) {
                reachEnd(id)
                return
            }
            showPosition(position)
            val speed = store.settings.speed
                .toDouble()
            val untilEnd = (end - position) / speed
            val untilWordChange = findNextWordChange()
                ?.let { change -> ((change - position) / speed).coerceAtLeast(minimumWordTick) }
                ?: positionTick
            delay(minOf(positionTick, untilEnd, untilWordChange))
        }
    }

    private fun showPosition(position: Duration) {
        val isWordHighlightEnabled = store.settings.isWordHighlightEnabled
        store.updateSession { session ->
            session.withPosition(
                position = position,
                isWordHighlightEnabled = isWordHighlightEnabled,
            )
        }
    }

    private fun findNextWordChange(): Duration? {
        if (!store.settings.isWordHighlightEnabled) return null
        return store.session?.findNextWordChange(position)
    }

    // Why: a chapter paused while it loaded counts as started when it first plays, not when it loads.
    private fun trackStartOnce(session: ListeningSessionModel) {
        if (hasTrackedStart) return
        hasTrackedStart = true
        tracker.trackStarted(session)
    }

    private fun prefetchNextSegment(current: ListeningSessionModel) {
        if (current.mode != ListeningModeModel.DAY_READING && !store.settings.isAutoNextEnabled) return
        val willReachNext = sleepTimer.willReachNextSegment(
            current = current,
            position = position,
        )
        if (willReachNext) prefetcher.prefetchNext(current)
    }

    private suspend fun onSleepTimerElapsed() {
        pause()
        sleepTimer.end(SLEEP_TIMER_COUNTDOWN)
    }

    private fun ListeningStatusModel.toPausedIfFinished(): ListeningStatusModel =
        if (this == ListeningStatusModel.FINISHED) ListeningStatusModel.PAUSED else this

    private companion object {
        const val SLEEP_TIMER_COUNTDOWN = "countdown"
    }
}
