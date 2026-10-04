package com.quare.bibleplanner.core.chapterlistening.domain.controller

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.usecase.GetChapterVerseTexts
import com.quare.bibleplanner.core.chapterlistening.domain.model.AudioInterruptionModel
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
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningVersionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.NowPlayingVerseModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechEngineEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechUtteranceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechVoicesModel
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningSettingsRepository
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.domain.service.SpeechEngine
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.EstimateListeningTimeUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetAdjacentListeningChapter
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningChapterTitle
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningVersion
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/*
 * Why: every call and every engine callback runs on the scope's single (main) thread, so the plain
 * vars below need no locking; the generation number drops the callbacks of an utterance that was
 * stopped before they arrived.
 */
internal class ChapterListeningControllerImpl(
    private val scope: CoroutineScope,
    private val speechEngine: SpeechEngine,
    private val mediaSession: ListeningMediaSession,
    private val settingsRepository: ChapterListeningSettingsRepository,
    private val getChapterVerseTexts: GetChapterVerseTexts,
    private val getListeningVersion: GetListeningVersion,
    private val getChapterListeningAccess: GetChapterListeningAccess,
    private val getAdjacentListeningChapter: GetAdjacentListeningChapter,
    private val getListeningChapterTitle: GetListeningChapterTitle,
    private val estimateListeningTime: EstimateListeningTimeUseCase,
    private val trackEvent: TrackEvent,
) : ChapterListeningController {
    override val isSupported: Boolean
        get() = speechEngine.isSupported

    override val state: StateFlow<ChapterListeningStateModel>
        field = MutableStateFlow<ChapterListeningStateModel>(
            ChapterListeningStateModel(
                session = null,
                settings = ChapterListeningSettingsModel(
                    speed = ChapterListeningSettingsModel.DEFAULT_SPEED,
                    voiceId = null,
                    isAutoNextEnabled = true,
                ),
                sleepTimer = ListeningSleepTimerModel.Off,
                voices = SpeechVoicesModel.Loading,
                selectedVoiceId = null,
                previewingVoiceId = null,
            ),
        )

    override val events: SharedFlow<ChapterListeningEventModel>
        field = MutableSharedFlow<ChapterListeningEventModel>(extraBufferCapacity = 1)

    private val chapterGap: Duration = 1.5.seconds
    private val sleepTimerTick: Duration = 1.seconds
    private val detachedStopDelay: Duration = 2.seconds
    private val voicesByLanguage = mutableMapOf<String, SpeechVoicesModel.Available>()
    private val chapterTitles = mutableMapOf<ChapterLocationModel, String>()
    private var generation = 0
    private var previewCount = 0
    private var spokenCharIndex = 0
    private var shouldResumeAfterPreview = false
    private var attachedReaders = 0
    private var loadJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var detachedStopJob: Job? = null

    private val session: ListeningSessionModel?
        get() = state.value.session

    private val ListeningSessionModel.isPlaying: Boolean
        get() = status == ListeningStatusModel.PLAYING || status == ListeningStatusModel.PREPARING

    init {
        scope.launch { settingsRepository.observe().collect(::applySettings) }
        scope.launch { speechEngine.events.collect(::handleEngineEvent) }
        scope.launch { mediaSession.commands.collect(::handleRemoteCommand) }
        scope.launch { mediaSession.interruptions.collect(::handleInterruption) }
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
            val version = getListeningVersion() ?: return@launchLoad
            openSegment(
                session = createSession(
                    mode = ListeningModeModel.CHAPTER,
                    day = null,
                    segment = toWholeChapter(chapter),
                    version = version,
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
            val version = getListeningVersion() ?: return@launchLoad
            openSegment(
                session = createSession(
                    mode = ListeningModeModel.DAY_READING,
                    day = day,
                    segment = segment,
                    version = version,
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
        sleepTimerJob?.cancel()
        detachedStopJob?.cancel()
        silenceEngine()
        mediaSession.deactivate()
        shouldResumeAfterPreview = false
        state.update {
            it.copy(
                session = null,
                sleepTimer = ListeningSleepTimerModel.Off,
                previewingVoiceId = null,
            )
        }
    }

    override fun setSpeed(speed: Float) {
        state.update { it.copy(settings = it.settings.copy(speed = speed)) }
        scope.launch { settingsRepository.setSpeed(speed) }
        restartIfPlaying()
    }

    override fun selectVoice(voiceId: String) {
        state.update {
            it.copy(
                settings = it.settings.copy(voiceId = voiceId),
                selectedVoiceId = voiceId,
            )
        }
        scope.launch { settingsRepository.setVoiceId(voiceId) }
        restartIfPlaying()
    }

    override fun previewVoice(
        voiceId: String,
        sampleText: String,
    ) {
        val current = session ?: return
        if (current.isPlaying) {
            pause()
            shouldResumeAfterPreview = true
        }
        previewCount++
        state.update { it.copy(previewingVoiceId = voiceId) }
        mediaSession.requestActivation()
        speechEngine.speak(
            utterances = listOf(
                SpeechUtteranceModel(
                    id = "$PREVIEW_PREFIX$previewCount",
                    text = sampleText,
                    leadingSilence = Duration.ZERO,
                ),
            ),
            voiceId = voiceId,
            languageTag = current.languageTag,
            speed = state.value.settings.speed,
        )
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

    override fun openVoiceSettings() {
        speechEngine.openVoiceSettings()
    }

    private fun launchLoad(block: suspend () -> Unit) {
        loadJob?.cancel()
        loadJob = scope.launch { block() }
    }

    private fun createSession(
        mode: ListeningModeModel,
        day: ListeningDayModel?,
        segment: ListeningSegmentModel,
        version: ListeningVersionModel,
        shouldForceCanonOrder: Boolean,
    ): ListeningSessionModel = ListeningSessionModel(
        mode = mode,
        day = day,
        segment = segment,
        bibleVersionId = version.id,
        languageTag = version.languageTag,
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
        silenceEngine()
        spokenCharIndex = 0
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
        val verses = loadVerses(
            bibleVersionId = session.bibleVersionId,
            segment = session.segment,
        )
        val voices = loadVoices(session.languageTag)
        val statusWhileLoading = this.session?.status
        val status = when {
            voices !is SpeechVoicesModel.Available -> ListeningStatusModel.VOICE_UNAVAILABLE

            verses.isEmpty() -> ListeningStatusModel.FINISHED

            // Why: a pause or an audio interruption that arrived while loading must win over the start request.
            statusWhileLoading == ListeningStatusModel.PAUSED ||
                statusWhileLoading == ListeningStatusModel.INTERRUPTED -> statusWhileLoading

            shouldPlay -> ListeningStatusModel.PREPARING

            else -> ListeningStatusModel.PAUSED
        }
        updateSession {
            it.copy(
                verses = verses,
                status = status,
            )
        }
        trackOpenedSegment(
            session = session,
            status = status,
        )
        if (status == ListeningStatusModel.PREPARING) {
            speakFrom(
                verseIndex = 0,
                charIndex = 0,
                leadingSilence = leadingSilence,
            )
        }
    }

    private fun trackOpenedSegment(
        session: ListeningSessionModel,
        status: ListeningStatusModel,
    ) {
        if (status == ListeningStatusModel.VOICE_UNAVAILABLE) {
            trackEvent(
                name = AnalyticsEventNames.CHAPTER_LISTENING_VOICE_UNAVAILABLE,
                params = mapOf(AnalyticsParams.LANGUAGE to session.languageTag),
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

    private suspend fun loadVerses(
        bibleVersionId: String,
        segment: ListeningSegmentModel,
    ): List<ListeningVerseModel> {
        val firstVerse = segment.startVerse ?: 0
        val lastVerse = segment.endVerse ?: Int.MAX_VALUE
        return getChapterVerseTexts(
            ChapterRef(
                bibleVersionId = bibleVersionId,
                bookId = segment.chapter.bookId,
                chapterNumber = segment.chapter.chapterNumber,
            ),
        ).entries
            .filter { (number, text) -> number in firstVerse..lastVerse && text.isNotBlank() }
            .sortedBy { (number, _) -> number }
            .map { (number, text) ->
                ListeningVerseModel(
                    number = number,
                    text = text,
                    wordCount = estimateListeningTime.countWords(text),
                )
            }
    }

    private suspend fun loadVoices(languageTag: String): SpeechVoicesModel {
        val voices = voicesByLanguage[languageTag] ?: run {
            state.update { it.copy(voices = SpeechVoicesModel.Loading) }
            speechEngine.loadVoices(languageTag)
        }
        if (voices is SpeechVoicesModel.Available) voicesByLanguage[languageTag] = voices
        state.update {
            it.copy(
                voices = voices,
                selectedVoiceId = resolveVoiceId(
                    voices = voices,
                    preferredVoiceId = it.settings.voiceId,
                ),
            )
        }
        return voices
    }

    private fun applySettings(settings: ChapterListeningSettingsModel) {
        state.update {
            it.copy(
                settings = settings,
                selectedVoiceId = resolveVoiceId(
                    voices = it.voices,
                    preferredVoiceId = settings.voiceId,
                ),
            )
        }
    }

    private fun resolveVoiceId(
        voices: SpeechVoicesModel,
        preferredVoiceId: String?,
    ): String? {
        val available = (voices as? SpeechVoicesModel.Available)?.voices ?: return null
        val preferred = available.find { it.id == preferredVoiceId }
        return (preferred ?: available.find { it.isEnhanced } ?: available.firstOrNull())?.id
    }

    private fun speakFrom(
        verseIndex: Int,
        charIndex: Int,
        leadingSilence: Duration,
    ) {
        val current = session ?: return
        val verse = current.verses.getOrNull(verseIndex) ?: return
        val startChar = charIndex.coerceIn(0, verse.text.length)
        if (verse.text.substring(startChar).isBlank()) {
            if (verseIndex == current.verses.lastIndex) {
                launchLoad { onSegmentFinished() }
            } else {
                speakFrom(
                    verseIndex = verseIndex + 1,
                    charIndex = 0,
                    leadingSilence = leadingSilence,
                )
            }
            return
        }
        if (!mediaSession.requestActivation()) {
            updateSession { it.copy(status = ListeningStatusModel.PAUSED) }
            return
        }
        generation++
        spokenCharIndex = startChar
        val utterances = (verseIndex..current.verses.lastIndex).map { index ->
            val offset = if (index == verseIndex) startChar else 0
            SpeechUtteranceModel(
                id = toVerseUtteranceId(
                    verseIndex = index,
                    offset = offset,
                ),
                text = current.verses[index].text.substring(offset),
                leadingSilence = if (index == verseIndex) leadingSilence else Duration.ZERO,
            )
        }
        speechEngine.speak(
            utterances = utterances,
            voiceId = state.value.selectedVoiceId,
            languageTag = current.languageTag,
            speed = state.value.settings.speed,
        )
        updateSession {
            it.copy(
                verseIndex = verseIndex,
                verseProgress = getVerseProgress(
                    verse = verse,
                    charIndex = startChar,
                ),
                status = ListeningStatusModel.PLAYING,
            )
        }
        ensureSleepTimerTicking()
    }

    private fun pause() {
        val current = session ?: return
        if (!current.isPlaying) return
        silenceEngine()
        updateSession { it.copy(status = ListeningStatusModel.PAUSED) }
    }

    private fun resume() {
        val current = session ?: return
        when (current.status) {
            ListeningStatusModel.PAUSED,
            ListeningStatusModel.INTERRUPTED,
            -> resumePausedSession(current)

            ListeningStatusModel.FINISHED -> speakFrom(
                verseIndex = 0,
                charIndex = 0,
                leadingSilence = Duration.ZERO,
            )

            ListeningStatusModel.VOICE_UNAVAILABLE -> launchLoad {
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
        // Why: verses are only empty while the segment loads, and the load starts speaking once it sees PREPARING.
        if (current.verses.isEmpty()) {
            updateSession { it.copy(status = ListeningStatusModel.PREPARING) }
        } else {
            resumeFromSpokenWord(current)
        }
    }

    private fun restartIfPlaying() {
        val current = session ?: return
        if (current.status == ListeningStatusModel.PLAYING) resumeFromSpokenWord(current)
    }

    private fun resumeFromSpokenWord(current: ListeningSessionModel) {
        val verse = current.verses.getOrNull(current.verseIndex) ?: return
        speakFrom(
            verseIndex = current.verseIndex,
            charIndex = findWordStart(
                text = verse.text,
                charIndex = spokenCharIndex,
            ),
            leadingSilence = Duration.ZERO,
        )
    }

    private fun findWordStart(
        text: String,
        charIndex: Int,
    ): Int {
        if (charIndex <= 0) return 0
        return text.lastIndexOf(' ', startIndex = (charIndex - 1).coerceAtMost(text.lastIndex)) + 1
    }

    private fun moveToVerse(verseIndex: Int) {
        val current = session ?: return
        if (verseIndex !in current.verses.indices) return
        if (current.isPlaying) {
            speakFrom(
                verseIndex = verseIndex,
                charIndex = 0,
                leadingSilence = Duration.ZERO,
            )
            return
        }
        spokenCharIndex = 0
        updateSession {
            it.copy(
                verseIndex = verseIndex,
                verseProgress = 0f,
                status = if (it.status == ListeningStatusModel.FINISHED) ListeningStatusModel.PAUSED else it.status,
            )
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
        return getAdjacentListeningChapter(
            chapter = current.segment.chapter,
            direction = direction,
            shouldForceCanonOrder = current.shouldForceCanonOrder,
        )?.let(::toWholeChapter)
    }

    private suspend fun isOpen(segment: ListeningSegmentModel): Boolean =
        getChapterListeningAccess(segment.chapter) is ChapterListeningAccessModel.Open

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
            silenceEngine()
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
        silenceEngine()
        updateSession {
            it.copy(
                verseIndex = it.verses.lastIndex.coerceAtLeast(0),
                verseProgress = 1f,
                status = ListeningStatusModel.FINISHED,
                finishedChapter = finishedChapter,
            )
        }
    }

    private fun silenceEngine() {
        generation++
        speechEngine.stop()
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

    private fun handleEngineEvent(event: SpeechEngineEvent) {
        if (event.utteranceId.startsWith(PREVIEW_PREFIX)) {
            handlePreviewEvent(event)
            return
        }
        val utterance = toVerseUtteranceOrNull(event.utteranceId)?.takeIf { it.generation == generation } ?: return
        val current = session ?: return
        val verse = current.verses.getOrNull(utterance.verseIndex) ?: return
        when (event) {
            is SpeechEngineEvent.Started -> showSpokenChar(
                verseIndex = utterance.verseIndex,
                verse = verse,
                charIndex = utterance.offset,
            )

            is SpeechEngineEvent.RangeStarted -> showSpokenChar(
                verseIndex = utterance.verseIndex,
                verse = verse,
                charIndex = utterance.offset + event.charIndex,
            )

            is SpeechEngineEvent.Finished -> if (utterance.verseIndex == current.verses.lastIndex) {
                launchLoad { onSegmentFinished() }
            }

            is SpeechEngineEvent.Failed -> {
                Logger.e { "Speech failed for ${current.segment.chapter} verse ${verse.number}" }
                silenceEngine()
                updateSession { it.copy(status = ListeningStatusModel.PAUSED) }
            }
        }
    }

    private fun showSpokenChar(
        verseIndex: Int,
        verse: ListeningVerseModel,
        charIndex: Int,
    ) {
        spokenCharIndex = charIndex
        updateSession {
            it.copy(
                verseIndex = verseIndex,
                verseProgress = getVerseProgress(
                    verse = verse,
                    charIndex = charIndex,
                ),
            )
        }
    }

    private fun handlePreviewEvent(event: SpeechEngineEvent) {
        if (event.utteranceId != "$PREVIEW_PREFIX$previewCount") return
        if (event !is SpeechEngineEvent.Finished && event !is SpeechEngineEvent.Failed) return
        state.update { it.copy(previewingVoiceId = null) }
        if (shouldResumeAfterPreview) {
            shouldResumeAfterPreview = false
            resume()
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

    private fun interrupt() {
        val current = session ?: return
        if (!current.isPlaying) return
        silenceEngine()
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
        val verseDurations = current.verses.map { verse ->
            estimateListeningTime.getDuration(
                wordCount = verse.wordCount,
                speed = state.settings.speed,
            )
        }
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

    private fun getVerseProgress(
        verse: ListeningVerseModel,
        charIndex: Int,
    ): Float = if (verse.text.isEmpty()) 0f else (charIndex.toFloat() / verse.text.length).coerceIn(0f, 1f)

    private fun toWholeChapter(chapter: ChapterLocationModel): ListeningSegmentModel = ListeningSegmentModel(
        chapter = chapter,
        startVerse = null,
        endVerse = null,
    )

    private fun toVerseUtteranceId(
        verseIndex: Int,
        offset: Int,
    ): String = listOf(VERSE_PREFIX, generation, verseIndex, offset).joinToString(ID_SEPARATOR)

    private fun toVerseUtteranceOrNull(utteranceId: String): VerseUtterance? {
        val parts = utteranceId.split(ID_SEPARATOR)
        if (parts.size != VERSE_ID_PARTS || parts.first() != VERSE_PREFIX) return null
        return VerseUtterance(
            generation = parts[1].toIntOrNull() ?: return null,
            verseIndex = parts[2].toIntOrNull() ?: return null,
            offset = parts[3].toIntOrNull() ?: return null,
        )
    }

    private data class VerseUtterance(
        val generation: Int,
        val verseIndex: Int,
        val offset: Int,
    )

    private companion object {
        const val PREVIEW_PREFIX = "preview-"
        const val VERSE_PREFIX = "verse"
        const val ID_SEPARATOR = ":"
        const val VERSE_ID_PARTS = 4
        const val RESTART_VERSE_PROGRESS = 0.25f
        const val SLEEP_TIMER_COUNTDOWN = "countdown"
    }
}
