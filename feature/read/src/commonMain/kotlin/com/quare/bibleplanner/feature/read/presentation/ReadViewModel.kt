package com.quare.bibleplanner.feature.read.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.PendingVerseFocusModel
import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
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
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.usecase.ObserveReaderSettings
import com.quare.bibleplanner.feature.read.domain.usecase.ReadChapterUseCases
import com.quare.bibleplanner.feature.read.domain.usecase.ReadStudyUseCases
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFocusAid
import com.quare.bibleplanner.feature.read.presentation.daycompletion.ReadDayCompletionFactory
import com.quare.bibleplanner.feature.read.presentation.factory.ObserveReadData
import com.quare.bibleplanner.feature.read.presentation.mapper.ReadUiStateMapper
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.model.VerseFocusUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerticalChapterCounts
import com.quare.bibleplanner.feature.read.presentation.study.ReadChapterStudyOpener
import com.quare.bibleplanner.feature.read.presentation.vertical.ReadVerticalChaptersFactory
import com.quare.bibleplanner.ui.utils.observe
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReadViewModel(
    private val route: ReadNavRoute,
    private val chapterUseCases: ReadChapterUseCases,
    private val studyUseCases: ReadStudyUseCases,
    private val chapterStudyOpener: ReadChapterStudyOpener,
    private val requestLoginNudgeIfNeeded: RequestLoginNudgeIfNeeded,
    private val downloaderFacade: BibleVersionDownloaderFacade,
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlow,
    private val requestDownloadNotificationPermission: RequestDownloadNotificationPermission,
    private val observeReaderSettings: ObserveReaderSettings,
    private val setReaderFocusAid: SetReaderFocusAid,
    private val toggleVerseSelection: ToggleVerseSelection,
    private val clearVerseSelection: ClearVerseSelection,
    private val navigator: Navigator,
    val platform: Platform,
    observeReadData: ObserveReadData,
    observeDayCompletionCandidates: ObserveDayCompletionCandidates,
    observeVerseSelection: ObserveVerseSelection,
    verticalChaptersFactory: ReadVerticalChaptersFactory,
    dayCompletionFactory: ReadDayCompletionFactory,
    uiStateMapper: ReadUiStateMapper,
    trackEvent: TrackEvent,
) : TrackedViewModel<ReadUiEvent>(trackEvent) {
    private val bookId = BookId.valueOf(route.bookId)
    private val bookStringResource = bookId.toBookNameResource()
    private val retryCount = MutableStateFlow(0)

    private val verticalChapters = verticalChaptersFactory.create(
        scope = viewModelScope,
        bookId = bookId,
        chapterNumber = route.chapterNumber,
        shouldForceCanonOrder = route.isFromBookDetails,
    )

    private val verseSelection: StateFlow<VerseSelection?> = observeVerseSelection()

    /*
     * Why: optimistic flips shown before the Room write re-emits; an entry is dropped once
     * dataFlow reports the same value, so a write that differs or never lands can't stick.
     */
    private val pendingReadOverrides = MutableStateFlow<Map<ChapterLocationModel, Boolean>>(emptyMap())

    private val dayCompletion = dayCompletionFactory.create(viewModelScope)

    private val isOpeningChapterStudy = MutableStateFlow(false)
    private val chaptersBeingMarked = mutableSetOf<ChapterLocationModel>()
    private val isChapterStudyBeside = MutableStateFlow(false)
    private val visibleChapter = MutableStateFlow<ChapterLocationModel?>(null)

    private val verseFocus = MutableStateFlow(
        route.targetVerseNumbers.takeIf { it.isNotEmpty() }?.let { verseNumbers ->
            VerseFocusUiModel(
                bookId = BookId.valueOf(route.bookId),
                chapterNumber = route.chapterNumber,
                verseNumbers = verseNumbers,
            )
        },
    )

    /*
     * Why: scoring the plan walks the whole read state, so it is paid while the user reads
     * rather than on the tap that ends the day.
     */
    private val dayCompletionCandidates: StateFlow<Map<ChapterLocationModel, PlanDayLocationModel>> =
        combine(
            verticalChapters.prepended,
            verticalChapters.appended,
            transform = ::getShownChapters,
        ).distinctUntilChanged()
            .flatMapLatest(observeDayCompletionCandidates::invoke)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap(),
            )

    private val dataFlow = combine(
        verticalChapters.prepended,
        verticalChapters.appended,
        retryCount,
    ) { prepended, appended, _ -> prepended to appended }
        .flatMapLatest { (prepended, appended) ->
            observeReadData(
                bookId = bookId,
                chapterNumber = route.chapterNumber,
                bookStringResource = bookStringResource,
                isInitiallyRead = route.isChapterRead,
                isFromBookDetails = route.isFromBookDetails,
                prependedChapters = prepended,
                appendedChapters = appended,
            ).map { data ->
                VerticalChapterCounts(
                    prepended = prepended.size,
                    appended = appended.size,
                ) to data
            }
        }

    val uiState: StateFlow<ReadUiState> = combine(
        combine(
            dataFlow,
            observeReaderSettings(),
            verseSelection,
            pendingReadOverrides,
            verticalChapters.requestedCounts,
        ) { (settledCounts, data), settings, selection, overrides, requestedCounts ->
            val reconciledOverrides = uiStateMapper.dropReconciledOverrides(
                overrides = overrides,
                data = data,
            )
            if (reconciledOverrides != overrides) pendingReadOverrides.update { reconciledOverrides }
            uiStateMapper.map(
                data = data,
                settings = settings,
                selection = selection,
                overrides = reconciledOverrides,
                isLoadingPreviousChapter = settings.isVerticalReadingEnabled &&
                    requestedCounts.prepended > settledCounts.prepended,
                isLoadingNextChapter = settings.isVerticalReadingEnabled &&
                    requestedCounts.appended > settledCounts.appended,
            )
        },
        dayCompletion.banner,
        verseFocus,
        isOpeningChapterStudy,
        isChapterStudyBeside,
    ) { state, banner, focus, isOpeningStudy, isStudyBeside ->
        state.copy(
            dayCompletionBanner = banner,
            verseFocus = focus,
            isOpeningChapterStudy = isOpeningStudy,
            isChapterStudyBeside = isStudyBeside,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = uiStateMapper.createLoadingState(
            bookId = bookId,
            bookStringResource = bookStringResource,
            chapterNumber = route.chapterNumber,
            isChapterRead = route.isChapterRead,
        ),
    )

    init {
        prefetchStudyQuotaForDaysAboutToFinish()
        observeVerticalReading()
        showStudyOfVisibleChapter()
        prefetchStudyStatusOfVisibleChapter()
        /*
         * Why: pushing is driven by the store, not the tap, so any way a selection starts opens
         * the panel; the navigator ignores a route already on the stack. Closing is never reactive.
         */
        observe(
            verseSelection
                .map { selection -> selection != null && isShowingChapter(selection.chapter) }
                .distinctUntilChanged()
                .filter { hasOwnSelection -> hasOwnSelection },
        ) {
            navigator.navigate(VerseSelectionNavRoute)
        }
        observe(studyUseCases.pendingVerseFocusStore.pending.filterNotNull(), ::focusPendingVerses)
    }

    private fun focusPendingVerses(pending: PendingVerseFocusModel) {
        if (!isShowingChapter(pending)) return
        studyUseCases.pendingVerseFocusStore.consume(pending)
        verseFocus.update {
            VerseFocusUiModel(
                bookId = pending.bookId,
                chapterNumber = pending.chapterNumber,
                verseNumbers = pending.verseNumbers,
            )
        }
    }

    private fun isShowingChapter(pending: PendingVerseFocusModel): Boolean =
        (uiState.value.content as? ReadContentUiState.Success)
            ?.chapters
            .orEmpty()
            .any { it.chapter.bookId == pending.bookId && it.chapter.chapterNumber == pending.chapterNumber }

    private fun openChapterStudy(event: ReadUiEvent.OnChapterStudyClick) {
        if (isOpeningChapterStudy.value) return
        isOpeningChapterStudy.update { true }
        viewModelScope.launch {
            chapterStudyOpener.open(
                scope = viewModelScope,
                target = ChapterStudyTargetModel(
                    bookId = event.bookId,
                    chapterNumber = event.chapterNumber,
                ),
            )
            isOpeningChapterStudy.update { false }
        }
    }

    private fun prefetchStudyQuotaForDaysAboutToFinish() {
        observe(
            dayCompletionCandidates
                .map { candidates -> candidates.values.toSet() }
                .distinctUntilChanged()
                .filter { days -> days.isNotEmpty() },
        ) { days ->
            days.forEach { day -> studyUseCases.prefetchDayStudyQuota(day) }
        }
    }

    private fun observeVerticalReading() {
        observe(
            observeReaderSettings()
                .map { it.isVerticalReadingEnabled }
                .distinctUntilChanged(),
        ) { isEnabled ->
            if (isEnabled) verticalChapters.enable() else verticalChapters.disable()
        }
    }

    /*
     * Why: a selection only means something over the chapter it was made in, and this reader is
     * cleared after the next one opens, by when the selection may already belong to that one.
     */
    override fun onCleared() {
        if (verseSelection.value?.let { isShowingChapter(it.chapter) } == true) {
            clearVerseSelection()
        }
        super.onCleared()
    }

    private fun getShownChapters(
        prepended: List<ReadNavigationSuggestionModel>,
        appended: List<ReadNavigationSuggestionModel>,
    ): List<ChapterLocationModel> {
        val routeChapter = ChapterLocationModel(
            bookId = bookId,
            chapterNumber = route.chapterNumber,
        )
        val suggestedChapters = (prepended + appended).map { suggestion ->
            ChapterLocationModel(
                bookId = suggestion.bookId,
                chapterNumber = suggestion.chapterNumber,
            )
        }
        return listOf(routeChapter) + suggestedChapters
    }

    private fun isShowingChapter(chapter: ChapterRef): Boolean = ChapterLocationModel(
        bookId = chapter.bookId,
        chapterNumber = chapter.chapterNumber,
    ) in getShownChapters(
        prepended = verticalChapters.prepended.value,
        appended = verticalChapters.appended.value,
    )

    override fun handleEvent(event: ReadUiEvent) {
        when (event) {
            ReadUiEvent.OnArrowBackClick -> navigator.navigateBack()

            ReadUiEvent.OnRetryClick -> retryCount.update { it + 1 }

            is ReadUiEvent.ToggleReadStatus -> toggleReadStatus(
                bookId = event.bookId,
                chapterNumber = event.chapterNumber,
                source = SOURCE_READER,
            )

            is ReadUiEvent.OnListeningMarkReadClick -> markListenedChapterRead(event)

            ReadUiEvent.OnDownloadSelectedVersionClick -> downloadSelectedVersion()

            ReadUiEvent.ManageBibleVersions -> navigator.navigate(BibleVersionSelectorRoute)

            is ReadUiEvent.OnNavigationSuggestionClick -> navigateToSuggestion(event.suggestion)

            is ReadUiEvent.OnVerseClick -> selectVerse(event)

            is ReadUiEvent.OnNoteIconClick -> openNote(event)

            ReadUiEvent.OnAppearanceClick -> navigator.navigate(ReaderAppearanceNavRoute)

            is ReadUiEvent.OnChapterStudyClick -> openChapterStudy(event)

            ReadUiEvent.OnRulerDismissClick -> dismissRuler()

            ReadUiEvent.OnReachedEnd -> appendNextChapter()

            ReadUiEvent.OnReachedStart -> prependPreviousChapter()

            ReadUiEvent.OnDayCompletionBannerDismissed -> dayCompletion.dismissBanner()

            ReadUiEvent.OnVerseFocusShown -> verseFocus.update { null }

            is ReadUiEvent.OnWidthClassChanged -> isChapterStudyBeside.update { event.isWide }

            is ReadUiEvent.OnVisibleChapterChanged -> visibleChapter.update {
                ChapterLocationModel(
                    bookId = event.bookId,
                    chapterNumber = event.chapterNumber,
                )
            }
        }
    }

    private fun showStudyOfVisibleChapter() {
        observe(observeVisibleChapter(isStudyBeside = true)) { chapter ->
            navigator.navigate(
                ChapterStudyNavRoute(
                    bookId = chapter.bookId.name,
                    chapterNumber = chapter.chapterNumber,
                    isCompanion = true,
                ),
            )
        }
    }

    // Why: collectLatest drops the request of a chapter the reader already scrolled past.
    private fun prefetchStudyStatusOfVisibleChapter() {
        viewModelScope.launch {
            observeVisibleChapter(isStudyBeside = false).collectLatest { chapter ->
                studyUseCases.prefetchChapterStudyStatus(
                    ChapterStudyTargetModel(
                        bookId = chapter.bookId,
                        chapterNumber = chapter.chapterNumber,
                    ),
                )
            }
        }
    }

    private fun observeVisibleChapter(isStudyBeside: Boolean): Flow<ChapterLocationModel> = combine(
        isChapterStudyBeside,
        visibleChapter.filterNotNull(),
    ) { isBeside, chapter -> chapter.takeIf { isBeside == isStudyBeside } }
        .filterNotNull()
        .distinctUntilChanged()

    private fun selectVerse(event: ReadUiEvent.OnVerseClick) {
        val selection = toggleVerseSelection(
            chapter = event.chapter,
            verseNumber = event.verseNumber,
        )
        val verseNumbers = selection?.verseNumbers.orEmpty()
        if (selection == null) {
            navigator.navigateBack()
        }
        trackEvent(
            name = AnalyticsEventNames.VERSE_SELECTION_TOGGLED,
            params = mapOf(
                AnalyticsParams.IS_SELECTED to (event.verseNumber in verseNumbers),
                AnalyticsParams.VERSE_COUNT to verseNumbers.size,
            ),
        )
    }

    private fun openNote(event: ReadUiEvent.OnNoteIconClick) {
        navigator.navigate(
            VerseNoteNavRoute(
                bibleVersionId = event.chapter.bibleVersionId,
                bookId = event.chapter.bookId.name,
                chapterNumber = event.chapter.chapterNumber,
                verseNumbers = event.noteMark.noteVerseNumbers,
                noteId = event.noteMark.noteId,
            ),
        )
    }

    /*
     * Why: the offer only ever marks; reading the stored state first and ignoring repeated taps keeps a
     * double tap, or an offer whose chapter was just read elsewhere, from toggling it back to unread.
     */
    private fun markListenedChapterRead(event: ReadUiEvent.OnListeningMarkReadClick) {
        val key = ChapterLocationModel(
            bookId = event.bookId,
            chapterNumber = event.chapterNumber,
        )
        if (!chaptersBeingMarked.add(key)) return
        viewModelScope.launch {
            try {
                val isRead = chapterUseCases.isWholeChapterRead(
                    chapterNumber = event.chapterNumber,
                    bookId = event.bookId,
                )
                if (!isRead) {
                    toggleReadStatus(
                        bookId = event.bookId,
                        chapterNumber = event.chapterNumber,
                        source = SOURCE_LISTENING_OFFER,
                    ).join()
                }
            } finally {
                chaptersBeingMarked.remove(key)
            }
        }
    }

    private fun toggleReadStatus(
        bookId: BookId,
        chapterNumber: Int,
        source: String,
    ): Job {
        val key = ChapterLocationModel(
            bookId = bookId,
            chapterNumber = chapterNumber,
        )
        val willBeRead = !isCurrentlyRead(key)
        pendingReadOverrides.update { it + (key to willBeRead) }
        val completedDay = dayCompletionCandidates.value[key]?.takeIf { willBeRead }
        if (completedDay != null) dayCompletion.present(completedDay)
        return viewModelScope.launch {
            val isRead = chapterUseCases.toggleWholeChapterReadStatus(
                bookId = bookId,
                chapterNumber = chapterNumber,
            )
            pendingReadOverrides.update { it + (key to isRead) }
            trackEvent(
                name = AnalyticsEventNames.CHAPTER_READ_TOGGLED,
                params = mapOf(
                    AnalyticsParams.BOOK_ID to bookId.name.lowercase(),
                    AnalyticsParams.CHAPTER_NUMBER to chapterNumber,
                    AnalyticsParams.IS_READ to isRead,
                    AnalyticsParams.SOURCE to source,
                ),
            )
            requestLoginNudgeIfNeeded()
            if (isRead && completedDay == null) {
                dayCompletion.checkChapter(
                    bookId = bookId,
                    chapterNumber = chapterNumber,
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

    private fun navigateToSuggestion(suggestion: ReadNavigationSuggestionModel) {
        trackEvent(
            name = AnalyticsEventNames.READING_SUGGESTION_CLICKED,
            params = mapOf(
                AnalyticsParams.DIRECTION to suggestion.toDirection(),
                AnalyticsParams.BOOK_ID to suggestion.bookId.name.lowercase(),
                AnalyticsParams.CHAPTER_NUMBER to suggestion.chapterNumber,
            ),
        )
        /*
         * Why: the selection ends with the chapter it was made in, and this reader is only cleared
         * after the next one has opened.
         */
        clearVerseSelection()
        viewModelScope.launch {
            navigator.navigateReplacingTop(
                ReadNavRoute(
                    bookId = suggestion.bookId.name,
                    chapterNumber = suggestion.chapterNumber,
                    isChapterRead = chapterUseCases.isWholeChapterRead(
                        chapterNumber = suggestion.chapterNumber,
                        bookId = suggestion.bookId,
                    ),
                    isFromBookDetails = route.isFromBookDetails,
                    targetVerseNumbers = emptyList(),
                ),
            )
        }
    }

    private fun appendNextChapter() {
        if (uiState.value.settings.isVerticalReadingEnabled) verticalChapters.appendNextChapter()
    }

    private fun prependPreviousChapter() {
        if (uiState.value.settings.isVerticalReadingEnabled) verticalChapters.prependPreviousChapter()
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

    private fun ReadNavigationSuggestionModel.toDirection(): String =
        if (this == uiState.value.header.navigationSuggestions.previous) DIRECTION_PREVIOUS else DIRECTION_NEXT

    private companion object {
        const val SOURCE_READER = "reader"
        const val SOURCE_RULER = "ruler"
        const val SOURCE_LISTENING_OFFER = "listening_offer"
        const val DIRECTION_PREVIOUS = "previous"
        const val DIRECTION_NEXT = "next"
    }
}
