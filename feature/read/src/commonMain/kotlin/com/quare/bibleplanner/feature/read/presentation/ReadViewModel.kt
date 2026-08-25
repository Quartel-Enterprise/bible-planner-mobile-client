package com.quare.bibleplanner.feature.read.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterRead
import com.quare.bibleplanner.core.books.domain.usecase.ToggleWholeChapterReadStatus
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetCompletedDayForChapter
import com.quare.bibleplanner.core.plan.domain.usecase.ObserveDayCompletionCandidates
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestDownloadNotificationPermission
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ClearVerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveVerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ToggleVerseSelection
import com.quare.bibleplanner.feature.daystudy.domain.usecase.PrefetchDayStudyQuota
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetNextChapter
import com.quare.bibleplanner.feature.read.domain.usecase.ObserveReaderSettings
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFocusAid
import com.quare.bibleplanner.feature.read.presentation.factory.ObserveReadData
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.theme.font.ReaderFont
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The reader owns the chapter and the tapping; what can be *done* with the tapped verses belongs to
 * the selection panel, which is its own entry. The two meet at the shared selection store: this
 * writes to it and pushes the panel's route the moment it stops being empty.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReadViewModel(
    private val route: ReadNavRoute,
    private val observeReadData: ObserveReadData,
    private val toggleWholeChapterReadStatus: ToggleWholeChapterReadStatus,
    private val isWholeChapterRead: IsWholeChapterRead,
    private val getCompletedDayForChapter: GetCompletedDayForChapter,
    private val observeDayCompletionCandidates: ObserveDayCompletionCandidates,
    private val prefetchDayStudyQuota: PrefetchDayStudyQuota,
    private val requestLoginNudgeIfNeeded: RequestLoginNudgeIfNeeded,
    private val downloaderFacade: BibleVersionDownloaderFacade,
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlow,
    private val requestDownloadNotificationPermission: RequestDownloadNotificationPermission,
    private val observeReaderSettings: ObserveReaderSettings,
    private val setReaderFocusAid: SetReaderFocusAid,
    private val getNextChapter: GetNextChapter,
    private val observeVerseSelection: ObserveVerseSelection,
    private val toggleVerseSelection: ToggleVerseSelection,
    private val clearVerseSelection: ClearVerseSelection,
    trackEvent: TrackEvent,
    val platform: Platform,
) : TrackedViewModel<ReadUiEvent>(trackEvent) {
    private val isAppendRequested = MutableStateFlow(false)
    private val nextChapter = MutableStateFlow<Loadable<ReadNavigationSuggestionModel?>>(Loadable.Loading)
    private val bookId = BookId.valueOf(route.bookId)
    private val bookStringResource = bookId.toBookNameResource()
    private val retryCount = MutableStateFlow(0)

    /**
     * The chapters vertical reading has pulled in behind this one. It grows one chapter at a time as
     * the reader reaches the end, so the scroll has no fixed limit, and empties when the setting goes
     * off or the reader lands on another chapter.
     */
    private val appendedChapters = MutableStateFlow<List<ReadNavigationSuggestionModel>>(emptyList())

    /**
     * Read-status flips the user just tapped, shown before the Room write that backs [dataFlow]
     * commits and re-emits. An entry is dropped once [dataFlow] reports the same value, so a write
     * that lands on a different value than guessed (or never lands) never leaves the UI stuck.
     */
    private val pendingReadOverrides = MutableStateFlow<Map<ChapterLocationModel, Boolean>>(emptyMap())

    private val _uiAction = MutableSharedFlow<ReadUiAction>()
    val uiAction: SharedFlow<ReadUiAction> = _uiAction

    /**
     * Which chapters on screen would finish a reading-plan day the moment they are marked read, kept
     * up to date while the reader is open. Scoring the plan costs a trip through the whole read
     * state, so it is paid while the user reads rather than on the tap that ends the day.
     */
    private val dayCompletionCandidates: StateFlow<Map<ChapterLocationModel, PlanDayLocationModel>> =
        appendedChapters
            .map { appended ->
                listOf(ChapterLocationModel(bookId = bookId, chapterNumber = route.chapterNumber)) +
                    appended.map { ChapterLocationModel(it.bookId, it.chapterNumber) }
            }.distinctUntilChanged()
            .flatMapLatest(observeDayCompletionCandidates::invoke)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap(),
            )

    private val dataFlow = combine(
        appendedChapters,
        retryCount,
    ) { chapters, _ -> chapters }
        .flatMapLatest { chapters ->
            observeReadData(
                bookId = bookId,
                chapterNumber = route.chapterNumber,
                bookStringResource = bookStringResource,
                isInitiallyRead = route.isChapterRead,
                isFromBookDetails = route.isFromBookDetails,
                appendedChapters = chapters,
            ).map { data -> chapters.size to data }
        }

    private val requestedChapterCount: Flow<Int> = combine(
        appendedChapters,
        isAppendRequested,
    ) { chapters, isRequested ->
        chapters.size + if (isRequested) 1 else 0
    }

    val uiState: StateFlow<ReadUiState> = combine(
        dataFlow,
        observeReaderSettings(),
        observeVerseSelection(),
        pendingReadOverrides,
        requestedChapterCount,
    ) { (settledChapterCount, data), settings, selection, overrides, requestedCount ->
        val reconciledOverrides = overrides.dropReconciledOverrides(data)
        if (reconciledOverrides != overrides) pendingReadOverrides.update { reconciledOverrides }
        data.toUiState(
            settings = settings,
            selection = selection,
            overrides = reconciledOverrides,
            isLoadingNextChapter = settings.isVerticalReadingEnabled && requestedCount > settledChapterCount,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = createLoadingState(),
    )

    init {
        prefetchStudyQuotaForDaysAboutToFinish()
        observeVerticalReading()
        /*
         * Pushing is driven by the store rather than by the tap so it survives any other way the
         * selection could start, and it is idempotent: the navigator ignores a route already on the
         * stack. Closing is never reactive — see the panel's own note on why.
         */
        observe(
            observeVerseSelection()
                .map { it != null }
                .distinctUntilChanged()
                .filter { hasSelection -> hasSelection },
        ) {
            _uiAction.emit(
                ReadUiAction.NavigateToRoute(
                    route = VerseSelectionNavRoute,
                    replace = false,
                ),
            )
        }
    }

    /**
     * The day's study quota is a network round trip, so it is asked for while the reader is still on
     * the chapter that would end the day. By the time the celebration opens, the answer is in hand.
     */
    private fun prefetchStudyQuotaForDaysAboutToFinish() {
        observe(
            dayCompletionCandidates
                .map { candidates -> candidates.values.toSet() }
                .distinctUntilChanged()
                .filter { days -> days.isNotEmpty() },
        ) { days ->
            days.forEach { day -> prefetchDayStudyQuota(day) }
        }
    }

    /**
     * The chapter after the last one on screen is resolved while the user is still reading, so
     * reaching the end appends it right away instead of waiting on a walk through the whole plan.
     * Nothing is kept, or looked up, once the setting is off.
     */
    private fun observeVerticalReading() {
        observe(
            observeReaderSettings()
                .map { it.isVerticalReadingEnabled }
                .distinctUntilChanged(),
        ) { isEnabled ->
            if (isEnabled) {
                resolveNextChapter()
            } else {
                appendedChapters.update { emptyList() }
                isAppendRequested.update { false }
                nextChapter.update { Loadable.Loading }
            }
        }
    }

    /** A selection only means something over the chapter it was made in. */
    override fun onCleared() {
        clearVerseSelection()
        super.onCleared()
    }

    override fun handleEvent(event: ReadUiEvent) {
        when (event) {
            ReadUiEvent.OnArrowBackClick -> emitAction(ReadUiAction.NavigateBack)

            ReadUiEvent.OnRetryClick -> retryCount.update { it + 1 }

            is ReadUiEvent.ToggleReadStatus -> toggleReadStatus(event)

            ReadUiEvent.OnDownloadSelectedVersionClick -> downloadSelectedVersion()

            ReadUiEvent.ManageBibleVersions -> {
                emitAction(
                    ReadUiAction.NavigateToRoute(
                        route = BibleVersionSelectorRoute,
                        replace = false,
                    ),
                )
            }

            is ReadUiEvent.OnNavigationSuggestionClick -> navigateToSuggestion(event.suggestion)

            is ReadUiEvent.OnVerseClick -> selectVerse(event)

            ReadUiEvent.OnAppearanceClick -> {
                emitAction(
                    ReadUiAction.NavigateToRoute(
                        route = ReaderAppearanceNavRoute,
                        replace = false,
                    ),
                )
            }

            ReadUiEvent.OnRulerDismissClick -> dismissRuler()

            ReadUiEvent.OnReachedEnd -> appendNextChapter()
        }
    }

    private fun selectVerse(event: ReadUiEvent.OnVerseClick) {
        val selection = toggleVerseSelection(
            chapter = event.chapter,
            verseNumber = event.verseNumber,
        )
        val verseNumbers = selection?.verseNumbers.orEmpty()
        if (selection == null) {
            // The last verse was deselected, so the panel has nothing left to act on.
            emitAction(ReadUiAction.NavigateBack)
        }
        trackEvent(
            name = AnalyticsEventNames.VERSE_SELECTION_TOGGLED,
            params = mapOf(
                AnalyticsParams.IS_SELECTED to (event.verseNumber in verseNumbers),
                AnalyticsParams.VERSE_COUNT to verseNumbers.size,
            ),
        )
    }

    private fun toggleReadStatus(event: ReadUiEvent.ToggleReadStatus) {
        val key = ChapterLocationModel(bookId = event.bookId, chapterNumber = event.chapterNumber)
        val willBeRead = !isCurrentlyRead(key)
        pendingReadOverrides.update { it + (key to willBeRead) }
        val completedDay = dayCompletionCandidates.value[key]?.takeIf { willBeRead }
        if (completedDay != null) emitAction(completedDay.toNavigationAction())
        viewModelScope.launch {
            val isRead = toggleWholeChapterReadStatus(
                bookId = event.bookId,
                chapterNumber = event.chapterNumber,
            )
            pendingReadOverrides.update { it + (key to isRead) }
            trackEvent(
                name = AnalyticsEventNames.CHAPTER_READ_TOGGLED,
                params = mapOf(
                    AnalyticsParams.BOOK_ID to event.bookId.name.lowercase(),
                    AnalyticsParams.CHAPTER_NUMBER to event.chapterNumber,
                    AnalyticsParams.IS_READ to isRead,
                    AnalyticsParams.SOURCE to SOURCE_READER,
                ),
            )
            requestLoginNudgeIfNeeded()
            if (isRead && completedDay == null) {
                checkDayCompletion(
                    bookId = event.bookId,
                    chapterNumber = event.chapterNumber,
                )
            }
        }
    }

    private fun isCurrentlyRead(key: ChapterLocationModel): Boolean {
        val state = uiState.value
        if (state.header.bookId == key.bookId && state.header.chapterNumber == key.chapterNumber) {
            return state.header.isChapterRead
        }
        return (state.content as? ReadContentUiState.Success)
            ?.chapters
            ?.firstOrNull { it.chapter.bookId == key.bookId && it.chapter.chapterNumber == key.chapterNumber }
            ?.isRead
            ?: false
    }

    /**
     * The safety net for a tap that beat [dayCompletionCandidates] to it — a cold screen, or a
     * chapter that was not on screen. It reaches the same sheet, just after the write instead of
     * with it. The reader never learns which plan day it was opened for, so either way the day is
     * looked up from the chapter itself: any chapter can be the one that finishes a day, no matter
     * which screen led here.
     */
    private suspend fun checkDayCompletion(
        bookId: BookId,
        chapterNumber: Int,
    ) {
        val day = getCompletedDayForChapter(
            bookId = bookId,
            chapterNumber = chapterNumber,
        ) ?: return
        _uiAction.emit(day.toNavigationAction())
    }

    private fun PlanDayLocationModel.toNavigationAction(): ReadUiAction.NavigateToRoute = ReadUiAction.NavigateToRoute(
        route = DayReadingCompleteNavRoute(
            dayNumber = dayNumber,
            weekNumber = weekNumber,
            readingPlanType = readingPlanType.name,
        ),
        replace = false,
    )

    private fun navigateToSuggestion(suggestion: ReadNavigationSuggestionModel) {
        trackEvent(
            name = AnalyticsEventNames.READING_SUGGESTION_CLICKED,
            params = mapOf(
                AnalyticsParams.DIRECTION to suggestion.toDirection(),
                AnalyticsParams.BOOK_ID to suggestion.bookId.name.lowercase(),
                AnalyticsParams.CHAPTER_NUMBER to suggestion.chapterNumber,
            ),
        )
        viewModelScope.launch {
            _uiAction.emit(
                ReadUiAction.NavigateToRoute(
                    route = ReadNavRoute(
                        bookId = suggestion.bookId.name,
                        chapterNumber = suggestion.chapterNumber,
                        isChapterRead = isWholeChapterRead(
                            chapterNumber = suggestion.chapterNumber,
                            bookId = suggestion.bookId,
                        ),
                        isFromBookDetails = route.isFromBookDetails,
                    ),
                    replace = true,
                ),
            )
        }
    }

    /**
     * The request stands until the reading order answers it, so an end reached before the lookup
     * settles is served the moment it does instead of stalling until the reader scrolls again.
     */
    private fun appendNextChapter() {
        if (!uiState.value.settings.isVerticalReadingEnabled) return
        isAppendRequested.update { true }
        consumeNextChapter()
    }

    private fun consumeNextChapter() {
        val resolvedNextChapter = nextChapter.value
        if (resolvedNextChapter !is Loadable.Loaded || !isAppendRequested.value) return
        isAppendRequested.update { false }
        val chapter = resolvedNextChapter.value ?: return
        nextChapter.update { Loadable.Loading }
        appendedChapters.update { it + chapter }
        viewModelScope.launch { resolveNextChapter() }
    }

    private suspend fun resolveNextChapter() {
        val lastChapter = appendedChapters.value.lastOrNull()
        nextChapter.update { Loadable.Loading }
        val next = getNextChapter(
            bookId = lastChapter?.bookId ?: bookId,
            chapterNumber = lastChapter?.chapterNumber ?: route.chapterNumber,
            shouldForceCanonOrder = route.isFromBookDetails,
        )
        nextChapter.update { Loadable.Loaded(next) }
        consumeNextChapter()
    }

    private fun dismissRuler() {
        viewModelScope.launch {
            setReaderFocusAid(ReaderFocusAid.NONE)
            trackEvent(
                name = AnalyticsEventNames.READER_FOCUS_AID_CHANGED,
                params = mapOf(
                    AnalyticsParams.FOCUS_AID to ReaderFocusAid.NONE.name.lowercase(),
                    AnalyticsParams.SOURCE to SOURCE_RULER,
                ),
            )
        }
    }

    private fun downloadSelectedVersion() {
        viewModelScope.launch {
            val versionId = getSelectedVersionIdFlow().first()
            val isResume = (uiState.value.content as? ReadContentUiState.Error.ChapterNotFound)
                ?.downloadStatus is DownloadStatusModel.InProgress.Paused
            downloaderFacade.downloadVersion(versionId)
            trackEvent(
                name = AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_STARTED,
                params = mapOf(
                    AnalyticsParams.VERSION_ID to versionId,
                    AnalyticsParams.IS_RESUME to isResume,
                    AnalyticsParams.SOURCE to SOURCE_READER,
                ),
            )
            requestDownloadNotificationPermission()
        }
    }

    private fun emitAction(action: ReadUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }

    private fun ReadDataUiModel.toUiState(
        settings: ReaderSettingsModel,
        selection: VerseSelection?,
        overrides: Map<ChapterLocationModel, Boolean>,
        isLoadingNextChapter: Boolean,
    ): ReadUiState = ReadUiState(
        header = header.withReadOverride(overrides),
        content = content.withSelection(selection).withReadOverrides(overrides),
        settings = settings,
        isLoadingNextChapter = isLoadingNextChapter,
    )

    private fun ReadHeaderUiModel.withReadOverride(overrides: Map<ChapterLocationModel, Boolean>): ReadHeaderUiModel {
        val override = overrides[ChapterLocationModel(bookId = bookId, chapterNumber = chapterNumber)] ?: return this
        return copy(isChapterRead = override)
    }

    private fun ReadContentUiState.withReadOverrides(
        overrides: Map<ChapterLocationModel, Boolean>,
    ): ReadContentUiState = if (this !is ReadContentUiState.Success || overrides.isEmpty()) {
        this
    } else {
        copy(chapters = chapters.map { chapter -> chapter.withReadOverride(overrides) })
    }

    private fun ReadChapterUiModel.withReadOverride(overrides: Map<ChapterLocationModel, Boolean>): ReadChapterUiModel {
        val key = ChapterLocationModel(bookId = chapter.bookId, chapterNumber = chapter.chapterNumber)
        val override = overrides[key] ?: return this
        return copy(isRead = override)
    }

    private fun Map<ChapterLocationModel, Boolean>.dropReconciledOverrides(
        data: ReadDataUiModel,
    ): Map<ChapterLocationModel, Boolean> =
        if (isEmpty()) this else filterNot { (key, override) -> data.isAuthoritativelyRead(key) == override }

    private fun ReadDataUiModel.isAuthoritativelyRead(key: ChapterLocationModel): Boolean? {
        if (header.bookId == key.bookId && header.chapterNumber == key.chapterNumber) return header.isChapterRead
        return (content as? ReadContentUiState.Success)
            ?.chapters
            ?.firstOrNull { it.chapter.bookId == key.bookId && it.chapter.chapterNumber == key.chapterNumber }
            ?.isRead
    }

    private fun ReadContentUiState.withSelection(selection: VerseSelection?): ReadContentUiState =
        if (this !is ReadContentUiState.Success) {
            this
        } else {
            copy(chapters = chapters.map { chapter -> chapter.withSelection(selection) })
        }

    private fun ReadChapterUiModel.withSelection(selection: VerseSelection?): ReadChapterUiModel {
        val selectedVerseNumbers = selection
            ?.takeIf { it.chapter == chapter }
            ?.verseNumbers
            .orEmpty()
        return copy(
            verses = verses.map { verse ->
                verse.copy(isSelected = verse.number in selectedVerseNumbers)
            },
        )
    }

    private fun ReadNavigationSuggestionModel.toDirection(): String =
        if (this == uiState.value.header.navigationSuggestions.previous) DIRECTION_PREVIOUS else DIRECTION_NEXT

    private fun createLoadingState(): ReadUiState = ReadUiState(
        header = ReadHeaderUiModel(
            bookId = bookId,
            bookStringResource = bookStringResource,
            chapterNumber = route.chapterNumber,
            isChapterRead = route.isChapterRead,
            navigationSuggestions = ReadNavigationSuggestionsModel(
                previous = null,
                next = null,
            ),
            versionAbbreviation = Loadable.Loading,
        ),
        content = ReadContentUiState.Loading,
        settings = ReaderSettingsModel(
            fontSizeSp = ReaderFontSize.DEFAULT,
            font = ReaderFont.LORA,
            isRulerEnabled = false,
            rulerLines = ReaderRulerLines.DEFAULT,
            isFocusedVerseEnabled = false,
            isVerticalReadingEnabled = false,
        ),
        isLoadingNextChapter = false,
    )

    private companion object {
        const val SOURCE_READER = "reader"
        const val SOURCE_RULER = "ruler"
        const val DIRECTION_PREVIOUS = "previous"
        const val DIRECTION_NEXT = "next"
    }
}
