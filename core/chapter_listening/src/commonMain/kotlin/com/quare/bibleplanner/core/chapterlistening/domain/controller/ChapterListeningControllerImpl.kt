package com.quare.bibleplanner.core.chapterlistening.domain.controller

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.usecase.GetChapterVerseTexts
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
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterAudioRepository
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningSettingsRepository
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetAdjacentListeningChapter
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningChapterTitle
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningVersion
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
    private val getListeningVersion: GetListeningVersion,
    private val getChapterListeningAccess: GetChapterListeningAccess,
    private val getAdjacentListeningChapter: GetAdjacentListeningChapter,
    private val getListeningChapterTitle: GetListeningChapterTitle,
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
    private var playbackJob: Job? = null
    private var prefetchJob: Job? = null
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
                    bibleVersionId = getListeningVersion(),
                    shouldForceCanonOrder = shouldForceCanonOrder,
                ),
                shouldPlay = true,
                leadingSilence = Duration.ZERO,
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
                    bibleVersionId = getListeningVersion(),
                    shouldForceCanonOrder = false,
                ),
                shouldPlay = true,
                leadingSilence = Duration.ZERO,
            )
        }
    }

    override fun followReader(chapter: ChapterLocationModel) {
        val current = session ?: return
        if (current.segment.chapter == chapter || current.lockedSegment?.chapter == chapter) return
        val daySegment = current.day?.segments?.find { it.chapter == chapter }
        val base = if (daySegment == null) {
            current.copy(
                mode = ListeningModeModel.CHAPTER,
                day = null,
            )
        } else {
            current
        }
        val target = daySegment ?: toWholeChapter(chapter)
        // Why: a chapter the person only opened to read must not stop the one they are listening to.
        launchLoad {
            if (!isOpen(target)) return@launchLoad
            openSegment(
                session = base.copy(
                    segment = target,
                    chapterChangeCause = ChapterChangeCause.READER,
                ),
                shouldPlay = current.isPlaying,
                leadingSilence = Duration.ZERO,
            )
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
                shouldPlay = true,
                leadingSilence = Duration.ZERO,
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

    override fun previousChapter() {
        moveChapter(ChapterDirectionModel.PREVIOUS)
    }

    override fun stop() {
        loadJob?.cancel()
        prefetchJob?.cancel()
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

    private fun launchLoad(block: suspend () -> Unit) {
        loadJob?.cancel()
        loadJob = scope.launch { block() }
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
        shouldPlay: Boolean,
        leadingSilence: Duration,
    ) {
        silencePlayer()
        audio = null
        position = Duration.ZERO
        state.update {
            it.copy(
                session = session.copy(
                    verses = emptyList(),
                    verseIndex = 0,
                    verseProgress = 0f,
                    status = ListeningStatusModel.PREPARING,
                    lockedSegment = null,
                ),
            )
        }
        val result = chapterAudioRepository.getChapterAudio(
            bibleVersionId = session.bibleVersionId,
            chapter = session.segment.chapter,
        )
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

            verses.isEmpty() -> ListeningStatusModel.FINISHED

            // Why: a pause or an audio interruption that arrived while loading must win over the start request.
            statusWhileLoading == ListeningStatusModel.PAUSED ||
                statusWhileLoading == ListeningStatusModel.INTERRUPTED -> statusWhileLoading

            shouldPlay -> ListeningStatusModel.PREPARING

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
        if (loadedAudio != null) {
            refresh(session)
            prefetchNextSegment()
        }
    }

    // Why: a cached chapter is checked once per launch for a corrected revision, which plays from the next time on.
    private fun refresh(session: ListeningSessionModel) {
        scope.launch {
            chapterAudioRepository.refresh(
                bibleVersionId = session.bibleVersionId,
                chapter = session.segment.chapter,
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

        ChapterAudioFailureModel.BUDGET_REACHED,
        ChapterAudioFailureModel.UNAVAILABLE,
        -> ListeningStatusModel.AUDIO_UNAVAILABLE
    }

    private fun trackOpenedSegment(
        session: ListeningSessionModel,
        result: ChapterAudioResultModel,
        status: ListeningStatusModel,
    ) {
        if (result is ChapterAudioResultModel.Failed) {
            trackEvent(
                name = AnalyticsEventNames.CHAPTER_LISTENING_AUDIO_UNAVAILABLE,
                params = toChapterParams(session) + mapOf(AnalyticsParams.REASON to result.failure.name.lowercase()),
            )
            return
        }
        if (status != ListeningStatusModel.PREPARING) return
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_STARTED,
            params =
                toChapterParams(session) + mapOf(AnalyticsParams.CAUSE to session.chapterChangeCause.name.lowercase()),
        )
    }

    private fun toChapterParams(session: ListeningSessionModel): Map<String, Any> = mapOf(
        AnalyticsParams.MODE to session.mode.key,
        AnalyticsParams.BOOK_ID to session.segment.chapter.bookId.name
            .lowercase(),
        AnalyticsParams.CHAPTER_NUMBER to session.segment.chapter.chapterNumber,
    )

    private fun applySettings(settings: ChapterListeningSettingsModel) {
        state.update { it.copy(settings = settings) }
    }

    /*
     * Why: the next chapter is downloaded (and generated, the first time anyone asks for it) while
     * this one plays, so moving on doesn't wait for it; a locked one is left alone.
     */
    private fun prefetchNextSegment() {
        val current = session ?: return
        if (current.mode != ListeningModeModel.DAY_READING && !state.value.settings.isAutoNextEnabled) return
        prefetchJob?.cancel()
        prefetchJob = scope.launch {
            suspendRunCatching { prefetch(current) }
                .onFailure { throwable -> Logger.w(throwable) { "Could not prefetch the next chapter audio" } }
        }
    }

    private suspend fun prefetch(current: ListeningSessionModel) {
        val next = findAdjacentSegment(
            current = current,
            direction = ChapterDirectionModel.NEXT,
        ) ?: return
        if (!isOpen(next)) return
        chapterAudioRepository.getChapterAudio(
            bibleVersionId = current.bibleVersionId,
            chapter = next.chapter,
        )
        chapterAudioRepository.refresh(
            bibleVersionId = current.bibleVersionId,
            chapter = next.chapter,
        )
    }

    // Why: a session that expired mid-listening picks up again once the person signs back in.
    private fun resumeAfterSignIn() {
        if (session?.status == ListeningStatusModel.NEEDS_SIGN_IN) resume()
    }

    private fun playFrom(
        start: Duration,
        leadingSilence: Duration,
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
                leadingSilence = Duration.ZERO,
            )

            ListeningStatusModel.NEEDS_CONNECTION,
            ListeningStatusModel.NEEDS_SIGN_IN,
            ListeningStatusModel.VERSION_UNAVAILABLE,
            ListeningStatusModel.AUDIO_UNAVAILABLE,
            -> launchLoad {
                openSegment(
                    session = current,
                    shouldPlay = true,
                    leadingSilence = Duration.ZERO,
                )
            }

            ListeningStatusModel.PLAYING,
            ListeningStatusModel.PREPARING,
            ListeningStatusModel.NEXT_LOCKED,
            -> Unit
        }
    }

    private fun resumePausedSession(current: ListeningSessionModel) {
        // Why: verses are only empty while the segment loads, and the load starts playing once it sees PREPARING.
        if (current.verses.isEmpty()) {
            updateSession { it.copy(status = ListeningStatusModel.PREPARING) }
        } else {
            playFrom(
                start = position,
                leadingSilence = Duration.ZERO,
            )
        }
    }

    private fun moveToVerse(verseIndex: Int) {
        val current = session ?: return
        val verse = current.verses.getOrNull(verseIndex) ?: return
        if (current.isPlaying) {
            playFrom(
                start = verse.start,
                leadingSilence = Duration.ZERO,
            )
            return
        }
        position = verse.start
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
                leadingSilence = Duration.ZERO,
            )
            return
        }
        position = start
        showPosition(start)
        updateSession {
            it.copy(status = if (it.status == ListeningStatusModel.FINISHED) ListeningStatusModel.PAUSED else it.status)
        }
    }

    private fun moveChapter(direction: ChapterDirectionModel) {
        val current = session ?: return
        launchLoad {
            val target = findAdjacentSegment(
                current = current,
                direction = direction,
            ) ?: return@launchLoad
            // Why: a skip from the notification or the headset cannot show the unlock, so it keeps the chapter playing.
            if (!isOpen(target)) {
                trackLocked(target)
                return@launchLoad
            }
            openSegment(
                session = current.copy(
                    segment = target,
                    chapterChangeCause = ChapterChangeCause.PLAYER,
                ),
                shouldPlay = current.isPlaying,
                leadingSilence = Duration.ZERO,
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
            params = mapOf(
                AnalyticsParams.BOOK_ID to segment.chapter.bookId.name
                    .lowercase(),
                AnalyticsParams.CHAPTER_NUMBER to segment.chapter.chapterNumber,
            ),
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
        openSegment(
            session = current.copy(
                segment = next,
                chapterChangeCause = ChapterChangeCause.PLAYER,
                finishedChapter = finishedChapter,
            ),
            shouldPlay = true,
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
            is ChapterAudioPlayerEvent.Failed -> onPlaybackFailed()
        }
    }

    // Why: a cached file the player can't decode would fail every retry, so it is downloaded again.
    private fun onPlaybackFailed() {
        val current = session ?: return
        Logger.e { "Chapter audio playback failed for ${current.segment.chapter}" }
        silencePlayer()
        audio = null
        updateSession { it.copy(status = ListeningStatusModel.AUDIO_UNAVAILABLE) }
        scope.launch {
            chapterAudioRepository.evict(
                bibleVersionId = current.bibleVersionId,
                chapter = current.segment.chapter,
            )
        }
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
        val speed = state.settings.speed.toDouble()
        val verseDurations = current.verses.map { verse -> (verse.end - verse.start) / speed }
        val verseElapsed =
            verseDurations.getOrElse(current.verseIndex) { Duration.ZERO } * current.verseProgress.toDouble()
        return NowPlayingModel(
            chapterTitle = chapterTitle,
            subtitle = current.bibleVersionId.uppercase(),
            verses = current.verses.mapIndexed { index, verse ->
                NowPlayingVerseModel(
                    number = verse.number,
                    duration = verseDurations[index],
                )
            },
            verseIndex = current.verseIndex,
            verseElapsed = verseElapsed.inWholeSeconds.seconds,
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
    }
}
