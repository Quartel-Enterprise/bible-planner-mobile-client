package com.quare.bibleplanner.core.chapterlistening.domain.controller

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.ListeningNowPlayingMapper
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.createListeningSession
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toPlaybackFailed
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toReaderSession
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toWholeChapterSegment
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.withCurrentWord
import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningStateModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningSettingsRepository
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.FindAdjacentListeningSegmentUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.isOpen
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/*
 * Why: every call and every player callback runs on the scope's single (main) thread, shared with the
 * collaborators below, so the plain vars need no locking.
 */
internal class ChapterListeningControllerImpl(
    private val scope: CoroutineScope,
    private val store: ChapterListeningStateStore,
    private val playback: ListeningPlayback,
    private val loader: ListeningSegmentLoader,
    private val sleepTimer: ListeningSleepTimer,
    private val prefetcher: ChapterAudioPrefetcher,
    private val cacheKeeper: ChapterAudioCacheKeeper,
    private val readerAttachments: ListeningReaderAttachments,
    private val mediaSession: ListeningMediaSession,
    private val settingsRepository: ChapterListeningSettingsRepository,
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlow,
    private val getChapterListeningAccess: GetChapterListeningAccess,
    private val findAdjacentSegment: FindAdjacentListeningSegmentUseCase,
    private val tracker: ChapterListeningAnalyticsTracker,
    nowPlayingMapper: ListeningNowPlayingMapper,
    observeAuthenticatedUserId: ObserveAuthenticatedUserId,
) : ChapterListeningController {
    override val isSupported: Boolean
        get() = playback.isSupported

    override val state: StateFlow<ChapterListeningStateModel> = store.state

    override val events: SharedFlow<ChapterListeningEventModel> = store.events

    private val chapterGap: Duration = 1.5.seconds
    private var loadJob: Job? = null
    private var moveJob: Job? = null

    private val session: ListeningSessionModel?
        get() = store.session

    init {
        scope.launch { settingsRepository.observe().collect(::applySettings) }
        scope.launch { playback.events.collect(::handlePlayerEvent) }
        scope.launch { mediaSession.commands.collect(::handleRemoteCommand) }
        scope.launch { mediaSession.interruptions.collect(::handleInterruption) }
        scope.launch {
            observeAuthenticatedUserId()
                .distinctUntilChanged()
                .filterNotNull()
                .collect { resumeAfterSignIn() }
        }
        scope.launch {
            state
                .map(nowPlayingMapper::toNowPlayingOrNull)
                .filterNotNull()
                .distinctUntilChanged()
                .collect(::publishNowPlaying)
        }
    }

    override fun startChapter(
        chapter: ChapterLocationModel,
        shouldForceCanonOrder: Boolean,
    ) {
        launchLoad {
            openSegment(
                session = createListeningSession(
                    mode = ListeningModeModel.CHAPTER,
                    day = null,
                    segment = chapter.toWholeChapterSegment(),
                    bibleVersionId = getSelectedVersionIdFlow().first(),
                    shouldForceCanonOrder = shouldForceCanonOrder,
                ),
            )
        }
    }

    override fun startDayReading(
        day: ListeningDayModel,
        chapter: ChapterLocationModel,
    ) {
        val segment = day.segments.find { it.chapter == chapter } ?: day.segments.firstOrNull() ?: return
        launchLoad {
            openSegment(
                session = createListeningSession(
                    mode = ListeningModeModel.DAY_READING,
                    day = day,
                    segment = segment,
                    bibleVersionId = getSelectedVersionIdFlow().first(),
                    shouldForceCanonOrder = false,
                ),
            )
        }
    }

    override fun followReader(chapter: ChapterLocationModel) {
        val current = session ?: return
        if (current.segment.chapter == chapter || current.lockedSegment?.chapter == chapter) return
        val followed = current.toReaderSession(chapter)
        // Why: a chapter the person only opened to read must not stop the one they are listening to.
        launchMove {
            if (!getChapterListeningAccess.isOpen(followed.segment)) return@launchMove
            // Why: read after the checks, so a pause made while they ran is kept.
            if (session?.isPlaying == true) {
                startLoad { openSegment(session = followed) }
            } else {
                showUnloaded(followed)
            }
        }
    }

    override fun continueLockedChapter() {
        val current = session ?: return
        val locked = current.lockedSegment ?: return
        launchLoad {
            openSegment(
                session = current.copy(
                    segment = locked,
                    chapterChangeCause = ChapterChangeCause.PLAYER,
                    lockedSegment = null,
                ),
            )
        }
    }

    override fun togglePlayPause() {
        val current = session ?: return
        if (current.isPlaying) playback.pause() else resume()
    }

    override fun nextVerse() {
        val current = session ?: return
        moveToVerse((current.verseIndex + 1).coerceAtMost(current.verses.lastIndex))
    }

    override fun previousVerse() {
        val current = session ?: return
        val target = if (current.verseProgress > RESTART_VERSE_PROGRESS) {
            current.verseIndex
        } else {
            (current.verseIndex - 1).coerceAtLeast(0)
        }
        moveToVerse(target)
    }

    override fun skipToVerse(verseIndex: Int) {
        moveToVerse(verseIndex)
    }

    override fun nextChapter() {
        moveChapter(ChapterDirectionModel.NEXT)
    }

    override fun previousChapter() {
        moveChapter(ChapterDirectionModel.PREVIOUS)
    }

    /*
     * Why: the player unlocks a chosen chapter first, so it only moves if the adjacent segment is still
     * that chapter (moving by segment, so a day reading split within one chapter still advances), and
     * plays it when the person unlocked it to listen (the video paused us).
     */
    override fun moveToAdjacentChapter(
        direction: ChapterDirectionModel,
        chapter: ChapterLocationModel,
        shouldPlay: Boolean,
    ) {
        val current = session ?: return
        launchMove {
            val target = findAdjacentSegment(
                current = current,
                direction = direction,
            )
            if (target?.chapter != chapter) return@launchMove
            moveToSegment(
                moved = current.copy(
                    segment = target,
                    chapterChangeCause = ChapterChangeCause.PLAYER,
                ),
                shouldPlay = shouldPlay,
            )
        }
    }

    override suspend fun getAdjacentChapter(direction: ChapterDirectionModel): ChapterLocationModel? {
        val current = session ?: return null
        return findAdjacentSegment(
            current = current,
            direction = direction,
        )?.chapter
    }

    override fun stop() {
        moveJob?.cancel()
        loadJob?.cancel()
        prefetcher.cancel()
        cacheKeeper.cancelRefresh()
        sleepTimer.cancel()
        readerAttachments.cancelPendingStop()
        playback.release()
        loader.forgetRetryStart()
        mediaSession.deactivate()
        store.update {
            it.copy(
                session = null,
                sleepTimer = ListeningSleepTimerModel.Off,
            )
        }
    }

    override fun setSpeed(speed: Float) {
        store.update { it.copy(settings = it.settings.copy(speed = speed)) }
        scope.launch { settingsRepository.setSpeed(speed) }
        playback.setSpeed(speed)
    }

    override fun setSleepTimer(option: ListeningSleepTimerOption) {
        store.update { it.copy(sleepTimer = option.toSleepTimerModel()) }
        playback.ensureSleepTimerTicking()
    }

    override fun setAutoNextEnabled(isEnabled: Boolean) {
        store.update { it.copy(settings = it.settings.copy(isAutoNextEnabled = isEnabled)) }
        scope.launch { settingsRepository.setAutoNextEnabled(isEnabled) }
    }

    override fun setWordHighlightEnabled(isEnabled: Boolean) {
        store.update { it.copy(settings = it.settings.copy(isWordHighlightEnabled = isEnabled)) }
        scope.launch { settingsRepository.setWordHighlightEnabled(isEnabled) }
        // Why: a paused chapter shows or hides its word right away instead of on the next play.
        store.updateSession { current ->
            current.withCurrentWord(
                position = playback.position,
                isWordHighlightEnabled = isEnabled,
            )
        }
    }

    override fun dismissFinishOffer() {
        store.updateSession { it.copy(finishedChapter = null) }
    }

    override fun attachReader() {
        readerAttachments.attach()
    }

    override fun detachReader() {
        readerAttachments.detach(::stop)
    }

    /*
     * Why: each chapter loaded is a server call that may generate audio and a download, so a chapter
     * only swiped or skipped to while paused waits until the person plays it.
     */
    private fun showUnloaded(session: ListeningSessionModel) {
        loadJob?.cancel()
        loader
            .reset(
                session = session,
                status = ListeningStatusModel.PAUSED,
            )?.let(cacheKeeper::refreshLeftChapter)
    }

    private fun launchLoad(block: suspend () -> Unit) {
        moveJob?.cancel()
        startLoad(block)
    }

    private fun startLoad(block: suspend () -> Unit) {
        loadJob?.cancel()
        loadJob = scope.launch { block() }
    }

    /*
     * Why: a chapter change is checked (is there one, is it unlocked) before it cancels the load in
     * progress, so a refused change leaves that load to finish instead of a session stuck loading.
     */
    private fun launchMove(block: suspend () -> Unit) {
        moveJob?.cancel()
        moveJob = scope.launch { block() }
    }

    private suspend fun openSegment(
        session: ListeningSessionModel,
        leadingSilence: Duration = Duration.ZERO,
    ) {
        val leftSession = loader.reset(
            session = session,
            status = ListeningStatusModel.PREPARING,
        )
        val status = loader.load(
            session = session,
            leftSession = leftSession,
        )
        if (status == ListeningStatusModel.PREPARING) {
            playback.playFrom(
                start = playback.position,
                leadingSilence = leadingSilence,
            )
        }
    }

    private fun applySettings(settings: ChapterListeningSettingsModel) {
        store.update { it.copy(settings = settings) }
    }

    /*
     * Why: a session that expired mid-listening picks up again once the person signs back in; the
     * account signed in may be another one, so its access is checked again first.
     */
    private fun resumeAfterSignIn() {
        val current = session ?: return
        if (current.status != ListeningStatusModel.NEEDS_SIGN_IN) return
        // Why: right after a sign-in the Pro status may not be known yet, so a closed chapter asks to unlock, not stop.
        launchMove {
            if (getChapterListeningAccess.isOpen(current.segment)) resume() else showLocked(current)
        }
    }

    fun loadAfterSegmentEnd() {
        launchLoad { onSegmentFinished() }
    }

    private fun resume() {
        val current = session ?: return
        when {
            current.status.isUnavailable -> launchLoad { openSegment(session = current) }

            current.status == ListeningStatusModel.PAUSED ||
                current.status == ListeningStatusModel.INTERRUPTED -> resumePausedSession(current)

            current.status == ListeningStatusModel.FINISHED -> playback.playFrom(
                start = current.verses.firstOrNull()?.start ?: Duration.ZERO,
            )
        }
    }

    private fun resumePausedSession(current: ListeningSessionModel) {
        // Why: a segment still loading starts playing once it sees PREPARING; one never loaded is loaded now.
        if (current.verses.isEmpty() && loadJob?.isActive == true) {
            store.updateSession { it.copy(status = ListeningStatusModel.PREPARING) }
        } else if (current.verses.isEmpty()) {
            loadUnloaded(current)
        } else {
            playback.playFrom(start = playback.position)
        }
    }

    // Why: a chapter moved to while paused wasn't unlocked yet, so playing it goes through the unlock first.
    private fun loadUnloaded(current: ListeningSessionModel) {
        launchMove {
            if (getChapterListeningAccess.isOpen(current.segment)) {
                startLoad { openSegment(session = current) }
                return@launchMove
            }
            showLocked(current)
        }
    }

    // Why: the locked state's unlock button goes through the gate, which checks the access again.
    private fun showLocked(current: ListeningSessionModel) {
        tracker.trackLocked(current.segment)
        store.updateSession {
            it.copy(
                status = ListeningStatusModel.NEXT_LOCKED,
                lockedSegment = current.segment,
            )
        }
    }

    private fun moveToVerse(verseIndex: Int) {
        val current = session ?: return
        val verse = current.verses.getOrNull(verseIndex) ?: return
        if (current.isPlaying) {
            playback.playFrom(start = verse.start)
        } else {
            playback.showPausedVerse(
                verseIndex = verseIndex,
                start = verse.start,
            )
        }
    }

    private fun seekTo(target: Duration) {
        val current = session ?: return
        val first = current.verses.firstOrNull() ?: return
        val start = target.coerceIn(first.start, current.verses.last().end)
        if (current.isPlaying) playback.playFrom(start = start) else playback.seekPaused(start)
    }

    private fun moveChapter(direction: ChapterDirectionModel) {
        val current = session ?: return
        launchMove {
            val target = findAdjacentSegment(
                current = current,
                direction = direction,
            ) ?: return@launchMove
            moveToSegment(
                moved = current.copy(
                    segment = target,
                    chapterChangeCause = ChapterChangeCause.PLAYER,
                ),
                shouldPlay = false,
            )
        }
    }

    // Why: runs inside the caller's move job, after the checks that decided the move.
    private suspend fun moveToSegment(
        moved: ListeningSessionModel,
        shouldPlay: Boolean,
    ) {
        /*
         * Why: a move the gate allowed (shouldPlay) is open already, even if the unlock couldn't be
         * saved. A skip from the notification or the headset cannot show the unlock, so it keeps a
         * playing chapter; while paused it only looks ahead, and the unlock is asked for on play.
         */
        if (!shouldPlay && session?.isPlaying != true) {
            showUnloaded(moved)
            return
        }
        if (!shouldPlay && !getChapterListeningAccess.isOpen(moved.segment)) {
            tracker.trackLocked(moved.segment)
            return
        }
        startLoad { openSegment(session = moved) }
    }

    private suspend fun onSegmentFinished() {
        val current = session ?: return
        val finishedChapter = current.segment.chapter
        tracker.trackCompleted(current)
        if (store.sleepTimer == ListeningSleepTimerModel.EndOfChapter) {
            store.update { it.copy(sleepTimer = ListeningSleepTimerModel.Off) }
            playback.finish(finishedChapter)
            sleepTimer.end(ListeningSleepTimerOption.END_OF_CHAPTER.key)
            return
        }
        val isAutoNext = current.mode == ListeningModeModel.DAY_READING || store.settings.isAutoNextEnabled
        val next = if (isAutoNext) {
            findAdjacentSegment(
                current = current,
                direction = ChapterDirectionModel.NEXT,
            )
        } else {
            null
        }
        if (next == null) {
            playback.finish(finishedChapter)
            return
        }
        if (!getChapterListeningAccess.isOpen(next)) {
            tracker.trackLocked(next)
            playback.silence()
            store.updateSession {
                it.copy(
                    status = ListeningStatusModel.NEXT_LOCKED,
                    lockedSegment = next,
                    finishedChapter = finishedChapter,
                )
            }
            return
        }
        val nextSession = current.copy(
            segment = next,
            chapterChangeCause = ChapterChangeCause.PLAYER,
            finishedChapter = finishedChapter,
        )
        // Why: a pause, the sleep timer or an interruption during the lookups above must not start the next chapter.
        if (session?.status != ListeningStatusModel.PLAYING) {
            showUnloaded(nextSession)
            return
        }
        openSegment(
            session = nextSession,
            leadingSilence = chapterGap,
        )
    }

    private fun handlePlayerEvent(event: ChapterAudioPlayerEvent) {
        when (event) {
            is ChapterAudioPlayerEvent.Ended -> playback.reachEnd(event.playbackId)
            is ChapterAudioPlayerEvent.Failed -> onPlaybackFailed(event.isFileUnreadable)
        }
    }

    private fun onPlaybackFailed(isFileUnreadable: Boolean) {
        val current = session ?: return
        Logger.e { "Chapter audio playback failed for ${current.segment.chapter}" }
        val failedAudioPath = playback.audio?.audioPath
        playback.silence()
        loader.rememberRetryStart(current.segment)
        playback.dropAudio()
        store.updateSession(ListeningSessionModel::toPlaybackFailed)
        tracker.trackPlaybackFailed(current)
        if (isFileUnreadable && failedAudioPath != null) cacheKeeper.evict(failedAudioPath)
    }

    private fun handleRemoteCommand(command: ListeningRemoteCommand) {
        when (command) {
            ListeningRemoteCommand.Play -> resume()
            ListeningRemoteCommand.Pause -> playback.pause()
            ListeningRemoteCommand.Stop -> stop()
            ListeningRemoteCommand.NextVerse -> nextVerse()
            ListeningRemoteCommand.PreviousVerse -> previousVerse()
            is ListeningRemoteCommand.SeekTo -> seekTo(command.position)
            ListeningRemoteCommand.NextChapter -> nextChapter()
            ListeningRemoteCommand.PreviousChapter -> previousChapter()
        }
    }

    private fun handleInterruption(interruption: AudioInterruptionModel) {
        when (interruption) {
            AudioInterruptionModel.BEGAN -> playback.interrupt()
            AudioInterruptionModel.ENDED -> resumeAfterInterruption()
            AudioInterruptionModel.PAUSE_REQUESTED -> playback.pause()
        }
    }

    private fun resumeAfterInterruption() {
        if (session?.status == ListeningStatusModel.INTERRUPTED) resume()
    }

    private fun publishNowPlaying(nowPlaying: NowPlayingModel) {
        // Why: a title lookup still suspended when the session stopped must not bring the system player back.
        if (session != null) mediaSession.update(nowPlaying)
    }

    private companion object {
        const val RESTART_VERSE_PROGRESS = 0.25f
    }
}
