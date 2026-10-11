package com.quare.bibleplanner.core.chapterlistening.domain.controller

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.usecase.GetChapterVerseTexts
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toListeningAnalyticsParams
import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioFailureModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioResultModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningStateModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningRemoteCommand
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterAudioRepository
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningSettingsRepository
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetAdjacentListeningChapter
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningChapterTitle
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningTimeUseCase
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/*
 * Why: every call and every player callback runs on the scope's single (main) thread, so the plain
 * vars below need no locking; the playback id drops the callbacks of a playback that was stopped
 * before they arrived.
 */
internal class ChapterListeningControllerImpl(
    private val scope: CoroutineScope,
    private val audioPlayer: ChapterAudioPlayer,
    private val mediaSession: ListeningMediaSession,
    private val settingsRepository: ChapterListeningSettingsRepository,
    private val chapterAudioRepository: ChapterAudioRepository,
    private val getChapterVerseTexts: GetChapterVerseTexts,
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlow,
    private val getChapterListeningAccess: GetChapterListeningAccess,
    private val getAdjacentListeningChapter: GetAdjacentListeningChapter,
    private val getListeningChapterTitle: GetListeningChapterTitle,
    private val getListeningTime: GetListeningTimeUseCase,
    private val trackEvent: TrackEvent,
    observeAuthenticatedUserId: ObserveAuthenticatedUserId,
) : ChapterListeningController {
    override val isSupported: Boolean
        get() = audioPlayer.isSupported

    override val state: StateFlow<ChapterListeningStateModel>
        field = MutableStateFlow<ChapterListeningStateModel>(
            ChapterListeningStateModel(
                session = null,
                settings = ChapterListeningSettingsModel(
                    speed = ChapterListeningSettingsModel.DEFAULT_SPEED,
                    isAutoNextEnabled = true,
                ),
                sleepTimer = ListeningSleepTimerModel.Off,
            ),
        )

    override val events: SharedFlow<ChapterListeningEventModel>
        field = MutableSharedFlow<ChapterListeningEventModel>(extraBufferCapacity = 1)

    private val chapterGap: Duration = 1.5.seconds
    private val positionTick: Duration = 200.milliseconds
    private val sleepTimerTick: Duration = 1.seconds
    private val detachedStopDelay: Duration = 2.seconds
    private val chapterTitles = mutableMapOf<ChapterLocationModel, String>()
    private var audio: ChapterAudioModel? = null
    private var position: Duration = Duration.ZERO
    private var playbackId = 0
    private var startedPlaybackId: Int? = null
    private var attachedReaders = 0
    private var loadJob: Job? = null
    private var moveJob: Job? = null
    private var playbackJob: Job? = null
    private var prefetchJob: Job? = null
    private var prefetchingChapter: ChapterLocationModel? = null
    private var refreshJob: Job? = null
    private var hasTrackedStart = false
    private var hasPrefetched = false
    private var sleepTimerJob: Job? = null
    private var detachedStopJob: Job? = null

    private val session: ListeningSessionModel?
        get() = state.value.session

    private val ListeningSessionModel.isPlaying: Boolean
        get() = status == ListeningStatusModel.PLAYING || status == ListeningStatusModel.PREPARING

    init {
        scope.launch { settingsRepository.observe().collect(::applySettings) }
        scope.launch { audioPlayer.events.collect(::handlePlayerEvent) }
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
                .map(::toNowPlayingOrNull)
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
                session = createSession(
                    mode = ListeningModeModel.CHAPTER,
                    day = null,
                    segment = toWholeChapter(chapter),
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
                session = createSession(
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
        val followed = toChapterSession(
            current = current,
            chapter = chapter,
            cause = ChapterChangeCause.READER,
        )
        // Why: a chapter the person only opened to read must not stop the one they are listening to.
        launchMove {
            if (!isOpen(followed.segment)) return@launchMove
            // Why: read after the checks, so a pause made while they ran is kept.
            if (session?.isPlaying == true) {
                startLoad {
                    openSegment(
                        session = followed,
                    )
                }
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
        if (current.isPlaying) pause() else resume()
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

    /*
     * Why: the player unlocks a chosen chapter first, so it opens that one rather than working the
     * next one out again, and plays it when the person unlocked it to listen (the video paused us).
     */
    override fun moveToChapter(
        chapter: ChapterLocationModel,
        shouldPlay: Boolean,
    ) {
        val current = session ?: return
        moveToSegment(
            moved = toChapterSession(
                current = current,
                chapter = chapter,
                cause = ChapterChangeCause.PLAYER,
            ),
            shouldPlay = shouldPlay,
        )
    }

    private fun toChapterSession(
        current: ListeningSessionModel,
        chapter: ChapterLocationModel,
        cause: ChapterChangeCause,
    ): ListeningSessionModel {
        val daySegment = current.day?.segments?.find { it.chapter == chapter }
        val base = if (daySegment == null) {
            current.copy(
                mode = ListeningModeModel.CHAPTER,
                day = null,
            )
        } else {
            current
        }
        return base.copy(
            segment = daySegment ?: toWholeChapter(chapter),
            chapterChangeCause = cause,
        )
    }

    override fun previousChapter() {
        moveChapter(ChapterDirectionModel.PREVIOUS)
    }

    override fun stop() {
        moveJob?.cancel()
        loadJob?.cancel()
        prefetchJob?.cancel()
        refreshJob?.cancel()
        sleepTimerJob?.cancel()
        detachedStopJob?.cancel()
        silencePlayer()
        audioPlayer.release()
        audio = null
        mediaSession.deactivate()
        state.update {
            it.copy(
                session = null,
                sleepTimer = ListeningSleepTimerModel.Off,
            )
        }
    }

    override fun setSpeed(speed: Float) {
        state.update { it.copy(settings = it.settings.copy(speed = speed)) }
        scope.launch { settingsRepository.setSpeed(speed) }
        if (startedPlaybackId == playbackId) audioPlayer.setSpeed(speed)
    }

    override fun setSleepTimer(option: ListeningSleepTimerOption) {
        val timer = when (option) {
            ListeningSleepTimerOption.OFF -> ListeningSleepTimerModel.Off

            ListeningSleepTimerOption.FIFTEEN_MINUTES -> ListeningSleepTimerModel.Countdown(
                option = option,
                remaining = 15.minutes,
            )

            ListeningSleepTimerOption.THIRTY_MINUTES -> ListeningSleepTimerModel.Countdown(
                option = option,
                remaining = 30.minutes,
            )

            ListeningSleepTimerOption.END_OF_CHAPTER -> ListeningSleepTimerModel.EndOfChapter
        }
        state.update { it.copy(sleepTimer = timer) }
        ensureSleepTimerTicking()
    }

    override fun setAutoNextEnabled(isEnabled: Boolean) {
        state.update { it.copy(settings = it.settings.copy(isAutoNextEnabled = isEnabled)) }
        scope.launch { settingsRepository.setAutoNextEnabled(isEnabled) }
    }

    override fun dismissFinishOffer() {
        updateSession { it.copy(finishedChapter = null) }
    }

    override fun attachReader() {
        attachedReaders++
        detachedStopJob?.cancel()
    }

    /*
     * Why: replacing the top reader with the next chapter detaches the old one only after the new
     * one attached, but the grace still covers a replace that lands the other way round.
     */
    override fun detachReader() {
        attachedReaders = (attachedReaders - 1).coerceAtLeast(0)
        if (attachedReaders > 0) return
        detachedStopJob?.cancel()
        detachedStopJob = scope.launch {
            delay(detachedStopDelay)
            if (attachedReaders == 0) stop()
        }
    }

    /*
     * Why: each chapter loaded is a server call that may generate audio and a download, so a chapter
     * only swiped or skipped to while paused waits until the person plays it.
     */
    private fun showUnloaded(session: ListeningSessionModel) {
        loadJob?.cancel()
        resetSession(
            session = session,
            status = ListeningStatusModel.PAUSED,
        )?.let(::refreshLeftChapter)
    }

    // Why: returns the session being left, whose revision is checked once leaving it can't compete with a download.
    private fun resetSession(
        session: ListeningSessionModel,
        status: ListeningStatusModel,
    ): ListeningSessionModel? {
        cancelPrefetchUnlessFor(session.segment.chapter)
        val left = findLeftSession(session.segment.chapter)
        silencePlayer()
        hasTrackedStart = false
        hasPrefetched = false
        audio = null
        position = Duration.ZERO
        state.update {
            it.copy(
                session = session.copy(
                    verses = emptyList(),
                    verseIndex = 0,
                    verseProgress = 0f,
                    status = status,
                    lockedSegment = null,
                ),
            )
        }
        return left
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

    private fun createSession(
        mode: ListeningModeModel,
        day: ListeningDayModel?,
        segment: ListeningSegmentModel,
        bibleVersionId: String,
        shouldForceCanonOrder: Boolean,
    ): ListeningSessionModel = ListeningSessionModel(
        mode = mode,
        day = day,
        segment = segment,
        bibleVersionId = bibleVersionId,
        shouldForceCanonOrder = shouldForceCanonOrder,
        verses = emptyList(),
        verseIndex = 0,
        verseProgress = 0f,
        status = ListeningStatusModel.PREPARING,
        chapterChangeCause = ChapterChangeCause.START,
        finishedChapter = null,
        lockedSegment = null,
    )

    private suspend fun openSegment(
        session: ListeningSessionModel,
        leadingSilence: Duration = Duration.ZERO,
    ) {
        val leftSession = resetSession(
            session = session,
            status = ListeningStatusModel.PREPARING,
        )
        val result = chapterAudioRepository.loadChapterAudio(
            bibleVersionId = session.bibleVersionId,
            chapter = session.segment.chapter,
        )
        // Why: checked only now, so a new revision of the left chapter doesn't compete with this download.
        leftSession?.let(::refreshLeftChapter)
        val loadedAudio = (result as? ChapterAudioResultModel.Ready)?.audio
        val verses = loadedAudio
            ?.let { chapterAudio ->
                toListeningVerses(
                    audio = chapterAudio,
                    session = session,
                )
            }.orEmpty()
        val statusWhileLoading = this.session?.status
        val status = when {
            result is ChapterAudioResultModel.Failed -> toStatus(result.failure)

            // Why: a manifest with none of the segment's verses has nothing to play, which is a broken audio.
            verses.isEmpty() -> ListeningStatusModel.AUDIO_UNAVAILABLE

            // Why: a play, pause or audio interruption that arrived while loading wins over how the load started.
            statusWhileLoading == ListeningStatusModel.PREPARING ||
                statusWhileLoading == ListeningStatusModel.INTERRUPTED -> statusWhileLoading

            else -> ListeningStatusModel.PAUSED
        }
        audio = loadedAudio
        position = verses.firstOrNull()?.start ?: Duration.ZERO
        updateSession {
            it.copy(
                verses = verses,
                status = status,
            )
        }
        trackOpenedSegment(
            session = session,
            result = result,
            status = status,
        )
        if (status == ListeningStatusModel.PREPARING) {
            playFrom(
                start = position,
                leadingSilence = leadingSilence,
            )
        }
    }

    private fun findLeftSession(nextChapter: ChapterLocationModel): ListeningSessionModel? {
        val left = session ?: return null
        return left.takeIf { audio != null && left.segment.chapter != nextChapter }
    }

    /*
     * Why: a cached chapter is checked once per launch for a corrected revision, which plays from the
     * next time on; it is checked once the session leaves it, so it never competes with its own playback.
     */
    private fun refreshLeftChapter(left: ListeningSessionModel) {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            chapterAudioRepository.refresh(
                bibleVersionId = left.bibleVersionId,
                chapter = left.segment.chapter,
            )
        }
    }

    private suspend fun toListeningVerses(
        audio: ChapterAudioModel,
        session: ListeningSessionModel,
    ): List<ListeningVerseModel> {
        val segment = session.segment
        val firstVerse = segment.startVerse ?: 0
        val lastVerse = segment.endVerse ?: Int.MAX_VALUE
        val texts = suspendRunCatching {
            getChapterVerseTexts(
                ChapterRef(
                    bibleVersionId = session.bibleVersionId,
                    bookId = segment.chapter.bookId,
                    chapterNumber = segment.chapter.chapterNumber,
                ),
            )
        }.getOrDefault(emptyMap())
        val verses = audio.verses.filter { verse -> verse.number in firstVerse..lastVerse }
        return verses.mapIndexed { index, verse ->
            val next = verses.getOrNull(index + 1)
            ListeningVerseModel(
                number = verse.number,
                text = texts[verse.number].orEmpty(),
                // Why: a whole chapter starts with its spoken title, and each verse with the heading read before it.
                start = if (index == 0 &&
                    segment.startVerse == null
                ) {
                    Duration.ZERO
                } else {
                    verse.headingStart ?: verse.start
                },
                end = when {
                    next != null -> next.headingStart ?: next.start
                    segment.endVerse == null -> audio.duration
                    else -> verse.end
                },
            )
        }
    }

    private fun toStatus(failure: ChapterAudioFailureModel): ListeningStatusModel = when (failure) {
        ChapterAudioFailureModel.NO_CONNECTION -> ListeningStatusModel.NEEDS_CONNECTION
        ChapterAudioFailureModel.NOT_SIGNED_IN -> ListeningStatusModel.NEEDS_SIGN_IN
        ChapterAudioFailureModel.VERSION_NOT_ENABLED -> ListeningStatusModel.VERSION_UNAVAILABLE
        ChapterAudioFailureModel.BUDGET_REACHED -> ListeningStatusModel.NARRATION_PAUSED
        ChapterAudioFailureModel.UNAVAILABLE -> ListeningStatusModel.AUDIO_UNAVAILABLE
    }

    private fun trackOpenedSegment(
        session: ListeningSessionModel,
        result: ChapterAudioResultModel,
        status: ListeningStatusModel,
    ) {
        val unavailableReason = when {
            result is ChapterAudioResultModel.Failed -> result.failure.name.lowercase()
            status == ListeningStatusModel.AUDIO_UNAVAILABLE -> NO_VERSES_REASON
            else -> null
        }
        if (unavailableReason == null) return
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_AUDIO_UNAVAILABLE,
            params = toChapterParams(session) + mapOf(AnalyticsParams.REASON to unavailableReason),
        )
    }

    // Why: a chapter paused while it loaded counts as started when it first plays, not when it loads.
    private fun trackStartOnce(session: ListeningSessionModel) {
        if (hasTrackedStart) return
        hasTrackedStart = true
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_STARTED,
            params =
                toChapterParams(session) + mapOf(AnalyticsParams.CAUSE to session.chapterChangeCause.name.lowercase()),
        )
    }

    private fun toChapterParams(session: ListeningSessionModel): Map<String, Any> =
        mapOf(AnalyticsParams.MODE to session.mode.key) + session.segment.chapter.toListeningAnalyticsParams()

    private fun applySettings(settings: ChapterListeningSettingsModel) {
        state.update { it.copy(settings = settings) }
    }

    /*
     * Why: the next chapter is downloaded (and generated, the first time anyone asks for it) while
     * this one plays, so moving on doesn't wait for it; a locked one is left alone, and its revision
     * is only checked once the session leaves it, so the check never runs during its own playback.
     */
    private fun prefetchNextSegment() {
        val current = session ?: return
        if (current.mode != ListeningModeModel.DAY_READING && !state.value.settings.isAutoNextEnabled) return
        prefetchJob?.cancel()
        prefetchingChapter = null
        prefetchJob = scope.launch {
            try {
                suspendRunCatching { prefetch(current) }
                    .onFailure { throwable -> Logger.w(throwable) { "Could not prefetch the next chapter audio" } }
            } finally {
                // Why: a cancelled job ends after its replacement started, and must not clear the new one's chapter.
                if (prefetchJob === coroutineContext[Job]) prefetchingChapter = null
            }
        }
    }

    // Why: a prefetch for a chapter that is no longer next would compete with the one now loading.
    private fun cancelPrefetchUnlessFor(chapter: ChapterLocationModel) {
        if (prefetchingChapter == chapter) return
        prefetchJob?.cancel()
        prefetchingChapter = null
    }

    private suspend fun prefetch(current: ListeningSessionModel) {
        val next = findAdjacentSegment(
            current = current,
            direction = ChapterDirectionModel.NEXT,
        ) ?: return
        prefetchingChapter = next.chapter
        if (!isOpen(next)) return
        chapterAudioRepository.loadChapterAudio(
            bibleVersionId = current.bibleVersionId,
            chapter = next.chapter,
        )
    }

    /*
     * Why: a session that expired mid-listening picks up again once the person signs back in; the
     * account signed in may be another one, so its access is checked again first.
     */
    private fun resumeAfterSignIn() {
        val current = session ?: return
        if (current.status != ListeningStatusModel.NEEDS_SIGN_IN) return
        launchMove {
            if (isOpen(current.segment)) resume() else stop()
        }
    }

    private fun playFrom(
        start: Duration,
        leadingSilence: Duration = Duration.ZERO,
    ) {
        val current = session ?: return
        val chapterAudio = audio ?: return
        if (current.verses.isEmpty()) return
        if (!mediaSession.requestActivation()) {
            updateSession { it.copy(status = ListeningStatusModel.PAUSED) }
            return
        }
        silencePlayer()
        val id = playbackId
        position = start
        showPosition(start)
        updateSession { it.copy(status = ListeningStatusModel.PLAYING) }
        trackStartOnce(current)
        // Why: the next chapter may be generated on the server, so it is fetched once this one actually plays.
        if (!hasPrefetched) {
            hasPrefetched = true
            prefetchNextSegment()
        }
        playbackJob = scope.launch {
            delay(leadingSilence)
            audioPlayer.play(
                audioPath = chapterAudio.audioPath,
                position = start,
                speed = state.value.settings.speed,
                playbackId = id,
            )
            startedPlaybackId = id
            followPlayback(id)
        }
        ensureSleepTimerTicking()
    }

    // Why: a segment that ends mid-chapter must stop exactly at its last verse, before the next one starts.
    private suspend fun followPlayback(id: Int) {
        while (id == playbackId) {
            position = audioPlayer.position
            val end = session?.verses?.lastOrNull()?.end ?: return
            if (position >= end) {
                reachEnd(id)
                return
            }
            showPosition(position)
            val untilEnd = (end - position) / state.value.settings.speed
                .toDouble()
            delay(minOf(positionTick, untilEnd))
        }
    }

    private fun reachEnd(id: Int) {
        if (id != playbackId) return
        silencePlayer()
        launchLoad { onSegmentFinished() }
    }

    private fun showPosition(position: Duration) {
        val current = session ?: return
        val verseIndex = current.verses.indexOfLast { verse -> verse.start <= position }.coerceAtLeast(0)
        val verse = current.verses.getOrNull(verseIndex) ?: return
        val length = verse.end - verse.start
        val progress = if (length > Duration.ZERO) ((position - verse.start) / length).toFloat() else 0f
        updateSession {
            it.copy(
                verseIndex = verseIndex,
                verseProgress = progress.coerceIn(0f, 1f),
            )
        }
    }

    // Why: an interrupted chapter that is paused for good (another app took the audio) must not resume after.
    private fun pause() {
        val current = session ?: return
        if (!current.isPlaying && current.status != ListeningStatusModel.INTERRUPTED) return
        silencePlayer()
        updateSession { it.copy(status = ListeningStatusModel.PAUSED) }
    }

    private fun resume() {
        val current = session ?: return
        when (current.status) {
            ListeningStatusModel.PAUSED,
            ListeningStatusModel.INTERRUPTED,
            -> resumePausedSession(current)

            ListeningStatusModel.FINISHED -> playFrom(
                start = current.verses.firstOrNull()?.start ?: Duration.ZERO,
            )

            ListeningStatusModel.NEEDS_CONNECTION,
            ListeningStatusModel.NEEDS_SIGN_IN,
            ListeningStatusModel.VERSION_UNAVAILABLE,
            ListeningStatusModel.AUDIO_UNAVAILABLE,
            ListeningStatusModel.NARRATION_PAUSED,
            -> launchLoad {
                openSegment(
                    session = current,
                )
            }

            ListeningStatusModel.PLAYING,
            ListeningStatusModel.PREPARING,
            ListeningStatusModel.NEXT_LOCKED,
            -> Unit
        }
    }

    private fun resumePausedSession(current: ListeningSessionModel) {
        // Why: a segment still loading starts playing once it sees PREPARING; one never loaded is loaded now.
        if (current.verses.isEmpty() && loadJob?.isActive == true) {
            updateSession { it.copy(status = ListeningStatusModel.PREPARING) }
        } else if (current.verses.isEmpty()) {
            loadUnloaded(current)
        } else {
            playFrom(
                start = position,
            )
        }
    }

    // Why: a chapter moved to while paused wasn't unlocked yet, so playing it goes through the unlock first.
    private fun loadUnloaded(current: ListeningSessionModel) {
        launchMove {
            if (isOpen(current.segment)) {
                startLoad {
                    openSegment(
                        session = current,
                    )
                }
                return@launchMove
            }
            trackLocked(current.segment)
            updateSession {
                it.copy(
                    status = ListeningStatusModel.NEXT_LOCKED,
                    lockedSegment = current.segment,
                )
            }
        }
    }

    private fun moveToVerse(verseIndex: Int) {
        val current = session ?: return
        val verse = current.verses.getOrNull(verseIndex) ?: return
        if (current.isPlaying) {
            playFrom(
                start = verse.start,
            )
            return
        }
        position = verse.start
        audioPlayer.seekTo(verse.start)
        updateSession {
            it.copy(
                verseIndex = verseIndex,
                verseProgress = 0f,
                status = if (it.status == ListeningStatusModel.FINISHED) ListeningStatusModel.PAUSED else it.status,
            )
        }
    }

    private fun seekTo(target: Duration) {
        val current = session ?: return
        val first = current.verses.firstOrNull() ?: return
        val start = target.coerceIn(first.start, current.verses.last().end)
        if (current.isPlaying) {
            playFrom(
                start = start,
            )
            return
        }
        position = start
        // Why: Android's lock screen shows the player's own position, so a paused seek moves it too.
        audioPlayer.seekTo(start)
        showPosition(start)
        updateSession {
            it.copy(status = if (it.status == ListeningStatusModel.FINISHED) ListeningStatusModel.PAUSED else it.status)
        }
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

    private fun moveToSegment(
        moved: ListeningSessionModel,
        shouldPlay: Boolean,
    ) {
        launchMove {
            /*
             * Why: a skip from the notification or the headset cannot show the unlock, so it keeps a
             * playing chapter; while paused it only looks ahead, and the unlock is asked for on play.
             */
            if (shouldPlay || session?.isPlaying == true) {
                if (!isOpen(moved.segment)) {
                    trackLocked(moved.segment)
                    return@launchMove
                }
                startLoad {
                    openSegment(
                        session = moved,
                    )
                }
            } else {
                showUnloaded(moved)
            }
        }
    }

    override suspend fun getAdjacentChapter(direction: ChapterDirectionModel): ChapterLocationModel? {
        val current = session ?: return null
        return findAdjacentSegment(
            current = current,
            direction = direction,
        )?.chapter
    }

    private suspend fun findAdjacentSegment(
        current: ListeningSessionModel,
        direction: ChapterDirectionModel,
    ): ListeningSegmentModel? {
        val day = current.day
        if (current.mode == ListeningModeModel.DAY_READING && day != null) {
            val index = day.segments.indexOf(current.segment)
            val step = if (direction == ChapterDirectionModel.NEXT) 1 else -1
            return day.segments.getOrNull(index + step)
        }
        return suspendRunCatching {
            getAdjacentListeningChapter(
                chapter = current.segment.chapter,
                direction = direction,
                shouldForceCanonOrder = current.shouldForceCanonOrder,
            )
        }.onFailure { throwable -> Logger.e(throwable) { "Could not find the $direction listening chapter" } }
            .getOrNull()
            ?.let(::toWholeChapter)
    }

    // Why: like the reader's gate, a failed access check lets the person listen; the server's budget caps the cost.
    private suspend fun isOpen(segment: ListeningSegmentModel): Boolean {
        val access = suspendRunCatching { getChapterListeningAccess(segment.chapter) }
            .onFailure { throwable -> Logger.e(throwable) { "Could not check the access to ${segment.chapter}" } }
            .getOrDefault(ChapterListeningAccessModel.Open)
        return access is ChapterListeningAccessModel.Open
    }

    private fun trackLocked(segment: ListeningSegmentModel) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_LOCKED,
            params = segment.chapter.toListeningAnalyticsParams(),
        )
    }

    private suspend fun onSegmentFinished() {
        val current = session ?: return
        val finishedChapter = current.segment.chapter
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_COMPLETED,
            params = toChapterParams(current),
        )
        if (state.value.sleepTimer == ListeningSleepTimerModel.EndOfChapter) {
            state.update { it.copy(sleepTimer = ListeningSleepTimerModel.Off) }
            finish(finishedChapter)
            endSleepTimer(ListeningSleepTimerOption.END_OF_CHAPTER.key)
            return
        }
        val isAutoNext = current.mode == ListeningModeModel.DAY_READING || state.value.settings.isAutoNextEnabled
        val next = if (isAutoNext) {
            findAdjacentSegment(
                current = current,
                direction = ChapterDirectionModel.NEXT,
            )
        } else {
            null
        }
        if (next == null) {
            finish(finishedChapter)
            return
        }
        if (!isOpen(next)) {
            trackLocked(next)
            silencePlayer()
            updateSession {
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

    private fun finish(finishedChapter: ChapterLocationModel) {
        silencePlayer()
        position = session?.verses?.firstOrNull()?.start ?: Duration.ZERO
        updateSession {
            it.copy(
                verseIndex = it.verses.lastIndex.coerceAtLeast(0),
                verseProgress = 1f,
                status = ListeningStatusModel.FINISHED,
                finishedChapter = finishedChapter,
            )
        }
    }

    private fun silencePlayer() {
        stopFollowingPlayback()
        audioPlayer.pause()
    }

    // Why: the position read here is where a later resume picks up, so it is taken before the player pauses.
    private fun stopFollowingPlayback() {
        if (startedPlaybackId == playbackId) position = audioPlayer.position
        playbackId++
        playbackJob?.cancel()
    }

    private fun ensureSleepTimerTicking() {
        if (state.value.sleepTimer !is ListeningSleepTimerModel.Countdown || sleepTimerJob?.isActive == true) return
        sleepTimerJob = scope.launch {
            while (true) {
                delay(sleepTimerTick)
                val current = state.value
                val timer = current.sleepTimer as? ListeningSleepTimerModel.Countdown ?: break
                if (current.session?.status != ListeningStatusModel.PLAYING) continue
                val remaining = timer.remaining - sleepTimerTick
                if (remaining > Duration.ZERO) {
                    state.update { it.copy(sleepTimer = timer.copy(remaining = remaining)) }
                    continue
                }
                state.update { it.copy(sleepTimer = ListeningSleepTimerModel.Off) }
                pause()
                endSleepTimer(SLEEP_TIMER_COUNTDOWN)
                break
            }
        }
    }

    private suspend fun endSleepTimer(option: String) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_SLEEP_TIMER_ENDED,
            params = mapOf(AnalyticsParams.OPTION to option),
        )
        events.emit(ChapterListeningEventModel.SleepTimerEnded)
    }

    private fun handlePlayerEvent(event: ChapterAudioPlayerEvent) {
        if (event.playbackId != playbackId) return
        when (event) {
            is ChapterAudioPlayerEvent.Ended -> reachEnd(event.playbackId)
            is ChapterAudioPlayerEvent.Failed -> onPlaybackFailed(event.isFileUnreadable)
        }
    }

    // Why: a cached file the player can't decode would fail every retry, so it is downloaded again.
    private fun onPlaybackFailed(isFileUnreadable: Boolean) {
        val current = session ?: return
        Logger.e { "Chapter audio playback failed for ${current.segment.chapter}" }
        val failedAudioPath = audio?.audioPath
        silencePlayer()
        audio = null
        // Why: the verses belong to the audio that failed, and the system player must not offer to seek in it.
        updateSession {
            it.copy(
                status = ListeningStatusModel.AUDIO_UNAVAILABLE,
                verses = emptyList(),
                verseIndex = 0,
                verseProgress = 0f,
            )
        }
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_AUDIO_UNAVAILABLE,
            params = toChapterParams(current) + mapOf(AnalyticsParams.REASON to PLAYBACK_FAILED_REASON),
        )
        if (isFileUnreadable && failedAudioPath != null) scope.launch { chapterAudioRepository.evict(failedAudioPath) }
    }

    private fun handleRemoteCommand(command: ListeningRemoteCommand) {
        when (command) {
            ListeningRemoteCommand.Play -> resume()
            ListeningRemoteCommand.Pause -> pause()
            ListeningRemoteCommand.Stop -> stop()
            ListeningRemoteCommand.NextVerse -> nextVerse()
            ListeningRemoteCommand.PreviousVerse -> previousVerse()
            is ListeningRemoteCommand.SkipToVerse -> moveToVerse(command.verseIndex)
            is ListeningRemoteCommand.SeekTo -> seekTo(command.position)
            ListeningRemoteCommand.NextChapter -> nextChapter()
            ListeningRemoteCommand.PreviousChapter -> previousChapter()
        }
    }

    private fun handleInterruption(interruption: AudioInterruptionModel) {
        when (interruption) {
            AudioInterruptionModel.BEGAN -> interrupt()
            AudioInterruptionModel.ENDED -> resumeAfterInterruption()
            AudioInterruptionModel.PAUSE_REQUESTED -> pause()
        }
    }

    private fun resumeAfterInterruption() {
        if (session?.status == ListeningStatusModel.INTERRUPTED) resume()
    }

    /*
     * Why: the system already silenced the player, and on Android pausing it here would give the audio
     * focus up for good, so the call ending would never be reported.
     */
    private fun interrupt() {
        val current = session ?: return
        if (!current.isPlaying) return
        stopFollowingPlayback()
        updateSession { it.copy(status = ListeningStatusModel.INTERRUPTED) }
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_INTERRUPTED,
            params = toChapterParams(current),
        )
    }

    private suspend fun toNowPlayingOrNull(state: ChapterListeningStateModel): NowPlayingModel? {
        val current = state.session ?: return null
        val chapterTitle = chapterTitles.getOrPut(current.segment.chapter) {
            getListeningChapterTitle(current.segment.chapter)
        }
        val time = getListeningTime(
            verses = current.verses,
            verseIndex = current.verseIndex,
            verseProgress = current.verseProgress,
            speed = state.settings.speed,
        )
        return NowPlayingModel(
            chapterTitle = chapterTitle,
            subtitle = current.bibleVersionId.uppercase(),
            verseNumber = current.verses.getOrNull(current.verseIndex)?.number,
            isAudioLoaded = current.verses.isNotEmpty(),
            // Why: whole seconds, so the 200 ms position ticks don't republish the system player every time.
            time = time.copy(elapsed = time.elapsed.inWholeSeconds.seconds),
            // Why: a chapter change passes through PREPARING, and reporting it as paused would drop the foreground service.
            isPlaying = current.isPlaying,
        )
    }

    private fun publishNowPlaying(nowPlaying: NowPlayingModel) {
        // Why: a title lookup still suspended when the session stopped must not bring the system player back.
        if (session != null) mediaSession.update(nowPlaying)
    }

    private fun updateSession(transform: (ListeningSessionModel) -> ListeningSessionModel) {
        state.update { current -> current.copy(session = current.session?.let(transform)) }
    }

    private fun toWholeChapter(chapter: ChapterLocationModel): ListeningSegmentModel = ListeningSegmentModel(
        chapter = chapter,
        startVerse = null,
        endVerse = null,
    )

    private companion object {
        const val RESTART_VERSE_PROGRESS = 0.25f
        const val SLEEP_TIMER_COUNTDOWN = "countdown"
        const val NO_VERSES_REASON = "no_verses"
        const val PLAYBACK_FAILED_REASON = "playback_failed"
    }
}
