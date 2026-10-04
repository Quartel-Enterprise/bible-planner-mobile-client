package com.quare.bibleplanner.feature.read.presentation.listening

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.repository.BooksRepository
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterRead
import com.quare.bibleplanner.core.chapterlistening.domain.controller.ChapterListeningController
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterChangeCause
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetTodayListeningDay
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ObserveIsChapterListeningEnabled
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.route.ChapterListeningPlayerNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.read.domain.usecase.ObserveReaderSettings
import com.quare.bibleplanner.feature.read.presentation.listening.mapper.ListeningPlayerUiModelMapper
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningFinishOfferUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningSurface
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiAction
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import com.quare.bibleplanner.ui.utils.observe
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReadListeningViewModel(
    private val route: ReadNavRoute,
    private val controller: ChapterListeningController,
    private val gate: ChapterListeningGate,
    private val playbackActions: ListeningPlaybackActions,
    private val followRequests: ListeningFollowRequests,
    private val navigator: Navigator,
    private val isWholeChapterRead: IsWholeChapterRead,
    private val booksRepository: BooksRepository,
    playerUiModelMapper: ListeningPlayerUiModelMapper,
    observeIsChapterListeningEnabled: ObserveIsChapterListeningEnabled,
    observeReaderSettings: ObserveReaderSettings,
    getTodayListeningDay: GetTodayListeningDay,
    trackEvent: TrackEvent,
) : TrackedViewModel<ReadListeningUiEvent>(trackEvent) {
    private val routeChapter = ChapterLocationModel(
        bookId = BookId.valueOf(route.bookId),
        chapterNumber = route.chapterNumber,
    )
    private val todayDay = MutableStateFlow<ListeningDayModel?>(null)
    private val isVerticalReading: StateFlow<Boolean> = observeReaderSettings()
        .map { settings -> settings.isVerticalReadingEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )
    private var isFollowingSession = followRequests.consume(routeChapter)

    val uiAction: SharedFlow<ReadListeningUiAction>
        field = MutableSharedFlow<ReadListeningUiAction>(extraBufferCapacity = 1)

    val uiState: StateFlow<ReadListeningUiState> = combine(
        observeIsChapterListeningEnabled(),
        todayDay,
        controller.state,
        observeFinishOffer(),
    ) { isEnabled, day, state, finishOffer ->
        ReadListeningUiState(
            isAvailable = isEnabled,
            speed = state.settings.speed,
            todayChapters = day?.segments?.map { segment -> segment.chapter }.orEmpty(),
            player = state.session?.let { session ->
                playerUiModelMapper.map(
                    session = session,
                    speed = state.settings.speed,
                )
            },
            finishOffer = finishOffer,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ReadListeningUiState(
            isAvailable = false,
            speed = ChapterListeningSettingsModel.DEFAULT_SPEED,
            todayChapters = emptyList(),
            player = null,
            finishOffer = null,
        ),
    )

    init {
        controller.attachReader()
        if (!isFollowingSession) controller.followReader(routeChapter)
        viewModelScope.launch {
            todayDay.update { suspendRunCatching { getTodayListeningDay(routeChapter) }.getOrNull() }
        }
        followPlayerChapter()
        observe(controller.events) { event ->
            when (event) {
                ChapterListeningEventModel.SleepTimerEnded -> uiAction.emit(ReadListeningUiAction.ShowSleepTimerEnded)
            }
        }
    }

    override fun onCleared() {
        controller.detachReader()
        super.onCleared()
    }

    override fun handleEvent(event: ReadListeningUiEvent) {
        when (event) {
            is ReadListeningUiEvent.OnListenClick -> listen(event)
            ReadListeningUiEvent.OnMiniPlayerClick -> navigator.navigate(ChapterListeningPlayerNavRoute)
            ReadListeningUiEvent.OnPlayPauseClick -> playbackActions.togglePlayPause(ListeningSurface.MINI_PLAYER)
            ReadListeningUiEvent.OnNextVerseClick -> controller.nextVerse()
            ReadListeningUiEvent.OnCloseClick -> controller.stop()
            ReadListeningUiEvent.OnVoiceSettingsClick -> controller.openVoiceSettings()
            ReadListeningUiEvent.OnUnlockNextClick -> playbackActions.unlockLockedChapter(viewModelScope)
            ReadListeningUiEvent.OnFinishOfferDismissClick -> controller.dismissFinishOffer()
            ReadListeningUiEvent.OnBackToVerseClick -> Unit
        }
    }

    private fun listen(event: ReadListeningUiEvent.OnListenClick) {
        val session = controller.state.value.session
        val isActive = session?.segment?.chapter == event.chapter
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_ENTRY_CLICKED,
            params = mapOf(
                AnalyticsParams.SOURCE to event.source.key,
                AnalyticsParams.IS_ACTIVE to isActive,
            ),
        )
        if (isActive) {
            navigator.navigate(ChapterListeningPlayerNavRoute)
            return
        }
        gate.request(
            scope = viewModelScope,
            chapter = event.chapter,
            onAllowed = { start(event.chapter) },
        )
    }

    private fun start(chapter: ChapterLocationModel) {
        val day = todayDay.value?.takeIf { day -> day.segments.any { segment -> segment.chapter == chapter } }
        if (day == null) {
            controller.startChapter(
                chapter = chapter,
                shouldForceCanonOrder = route.isFromBookDetails,
            )
        } else {
            controller.startDayReading(
                day = day,
                chapter = chapter,
            )
        }
    }

    /*
     * Why: only a reader that was showing the session's chapter follows it, and only when the player
     * moved on by itself; a reader the person opened elsewhere keeps its chapter.
     */
    private fun followPlayerChapter() {
        observe(
            controller.state
                .mapNotNull { state -> state.session }
                .map { session -> session.segment.chapter to session.chapterChangeCause }
                .distinctUntilChanged(),
        ) { (chapter, cause) ->
            if (chapter == routeChapter) {
                isFollowingSession = true
                return@observe
            }
            val shouldFollow = isFollowingSession && cause == ChapterChangeCause.PLAYER && !isVerticalReading.value
            isFollowingSession = false
            if (shouldFollow) openChapter(chapter)
        }
    }

    private suspend fun openChapter(chapter: ChapterLocationModel) {
        followRequests.request(chapter)
        navigator.navigateReplacing(
            current = route,
            route = ReadNavRoute(
                bookId = chapter.bookId.name,
                chapterNumber = chapter.chapterNumber,
                isChapterRead = isWholeChapterRead(
                    chapterNumber = chapter.chapterNumber,
                    bookId = chapter.bookId,
                ),
                isFromBookDetails = route.isFromBookDetails,
                targetVerseNumbers = emptyList(),
            ),
        )
    }

    private fun observeFinishOffer(): Flow<ListeningFinishOfferUiModel?> = controller.state
        .map { state -> state.session?.let(::toFinishOffer) }
        .distinctUntilChanged()
        .flatMapLatest { offer ->
            if (offer == null) {
                flowOf(null)
            } else {
                observeIsChapterRead(offer.chapter).map { isRead -> offer.takeIf { !isRead } }
            }
        }

    private fun toFinishOffer(session: ListeningSessionModel): ListeningFinishOfferUiModel? {
        val finishedChapter = session.finishedChapter ?: return null
        return ListeningFinishOfferUiModel(
            chapter = finishedChapter,
            playingChapter = session.segment.chapter.takeIf { chapter -> chapter != finishedChapter },
        )
    }

    private fun observeIsChapterRead(chapter: ChapterLocationModel): Flow<Boolean> = booksRepository
        .getBookByIdFlow(chapter.bookId)
        .map { book -> book?.chapters?.find { it.number == chapter.chapterNumber }?.isRead == true }
        .distinctUntilChanged()
}
