package com.quare.bibleplanner.feature.read.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterRead
import com.quare.bibleplanner.core.books.domain.usecase.ToggleWholeChapterReadStatus
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.PendingVerseFocusModel
import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallEntrySource
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockNavRoute
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetCompletedDayForChapter
import com.quare.bibleplanner.core.plan.domain.usecase.ObserveDayCompletionCandidates
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.ObserveStudySuggestionSettings
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestDownloadNotificationPermission
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ClearVerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveVerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ToggleVerseSelection
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderRulerLines
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetNextChapter
import com.quare.bibleplanner.feature.read.domain.usecase.GetPreviousChapter
import com.quare.bibleplanner.feature.read.domain.usecase.ObserveReaderSettings
import com.quare.bibleplanner.feature.read.domain.usecase.ReadStudyUseCases
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFocusAid
import com.quare.bibleplanner.feature.read.presentation.factory.ObserveReadData
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.model.VerseFocusUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerticalChapterCounts
import com.quare.bibleplanner.ui.theme.font.ReaderFont
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
class ReadViewModel(
    private val route: ReadNavRoute,
    private val toggleWholeChapterReadStatus: ToggleWholeChapterReadStatus,
    private val isWholeChapterRead: IsWholeChapterRead,
    private val getCompletedDayForChapter: GetCompletedDayForChapter,
    private val studyUseCases: ReadStudyUseCases,
    private val requestLoginNudgeIfNeeded: RequestLoginNudgeIfNeeded,
    private val downloaderFacade: BibleVersionDownloaderFacade,
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlow,
    private val requestDownloadNotificationPermission: RequestDownloadNotificationPermission,
    private val observeReaderSettings: ObserveReaderSettings,
    private val setReaderFocusAid: SetReaderFocusAid,
    private val getNextChapter: GetNextChapter,
    private val getPreviousChapter: GetPreviousChapter,
    private val toggleVerseSelection: ToggleVerseSelection,
    private val clearVerseSelection: ClearVerseSelection,
    private val navigator: Navigator,
    val platform: Platform,
    observeReadData: ObserveReadData,
    observeDayCompletionCandidates: ObserveDayCompletionCandidates,
    observeStudySuggestionSettings: ObserveStudySuggestionSettings,
    observeVerseSelection: ObserveVerseSelection,
    trackEvent: TrackEvent,
) : TrackedViewModel<ReadUiEvent>(trackEvent) {
    private val isAppendRequested = MutableStateFlow(false)
    private val isPrependRequested = MutableStateFlow(false)
    private val nextChapter = MutableStateFlow<Loadable<ReadNavigationSuggestionModel?>>(Loadable.Loading)
    private val previousChapter = MutableStateFlow<Loadable<ReadNavigationSuggestionModel?>>(Loadable.Loading)
    private val bookId = BookId.valueOf(route.bookId)
    private val bookStringResource = bookId.toBookNameResource()
    private val retryCount = MutableStateFlow(0)

    private val appendedChapters = MutableStateFlow<List<ReadNavigationSuggestionModel>>(emptyList())

    private val prependedChapters = MutableStateFlow<List<ReadNavigationSuggestionModel>>(emptyList())

    private val verseSelection: StateFlow<VerseSelection?> = observeVerseSelection()

    /*
     * Why: optimistic flips shown before the Room write re-emits; an entry is dropped once
     * dataFlow reports the same value, so a write that differs or never lands can't stick.
     */
    private val pendingReadOverrides = MutableStateFlow<Map<ChapterLocationModel, Boolean>>(emptyMap())

    private val dayCompletionBanner = MutableStateFlow<PlanDayLocationModel?>(null)

    private val isOpeningChapterStudy = MutableStateFlow(false)
    private var observeRewardedChapterStudyJob: Job? = null
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
     * Why: SharingStarted.Eagerly keeps the setting warm so the tap that ends a day never
     * waits on the preference.
     */
    private val studySuggestionSettings: StateFlow<StudySuggestionSettingsModel?> =
        observeStudySuggestionSettings()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

    /*
     * Why: scoring the plan walks the whole read state, so it is paid while the user reads
     * rather than on the tap that ends the day.
     */
    private val dayCompletionCandidates: StateFlow<Map<ChapterLocationModel, PlanDayLocationModel>> =
        combine(
            prependedChapters,
            appendedChapters,
            transform = ::getShownChapters,
        ).distinctUntilChanged()
            .flatMapLatest(observeDayCompletionCandidates::invoke)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap(),
            )

    private val dataFlow = combine(
        prependedChapters,
        appendedChapters,
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

    private val requestedChapterCounts: Flow<VerticalChapterCounts> = combine(
        prependedChapters,
        appendedChapters,
        isPrependRequested,
        isAppendRequested,
    ) { prepended, appended, isPrependPending, isAppendPending ->
        VerticalChapterCounts(
            prepended = prepended.size + if (isPrependPending) 1 else 0,
            appended = appended.size + if (isAppendPending) 1 else 0,
        )
    }

    val uiState: StateFlow<ReadUiState> = combine(
        combine(
            dataFlow,
            observeReaderSettings(),
            verseSelection,
            pendingReadOverrides,
            requestedChapterCounts,
        ) { (settledCounts, data), settings, selection, overrides, requestedCounts ->
            val reconciledOverrides = overrides.dropReconciledOverrides(data)
            if (reconciledOverrides != overrides) pendingReadOverrides.update { reconciledOverrides }
            data.toUiState(
                settings = settings,
                selection = selection,
                overrides = reconciledOverrides,
                isLoadingPreviousChapter = settings.isVerticalReadingEnabled &&
                    requestedCounts.prepended > settledCounts.prepended,
                isLoadingNextChapter = settings.isVerticalReadingEnabled &&
                    requestedCounts.appended > settledCounts.appended,
            )
        },
        dayCompletionBanner,
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
        initialValue = createLoadingState(),
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
            val target = ChapterStudyTargetModel(
                bookId = event.bookId,
                chapterNumber = event.chapterNumber,
            )
            val access = suspendRunCatching {
                studyUseCases.getChapterStudyAccess(target)
            }.getOrDefault(ChapterStudyAccessModel.OPEN)
            when (access) {
                ChapterStudyAccessModel.OPEN -> navigator.navigate(target.toChapterStudyNavRoute())

                ChapterStudyAccessModel.LOGIN_REQUIRED -> navigator.navigate(
                    LoginWarningNavRoute(LoginWarningReason.ChapterStudy.key),
                )

                ChapterStudyAccessModel.LIMIT_REACHED -> onChapterStudyLimitReached(target)
            }
            isOpeningChapterStudy.update { false }
        }
    }

    private suspend fun onChapterStudyLimitReached(target: ChapterStudyTargetModel) {
        if (studyUseCases.chapterStudyGenerationCoordinator.hasUnservedReward(target)) {
            openRewardedChapterStudy(target)
            return
        }
        val rewardedRemainingToday = suspendRunCatching { studyUseCases.getChapterStudyQuota(target) }
            .getOrNull()
            ?.rewardedRemainingToday
            ?: 0
        if (!studyUseCases.prepareRewardedUnlockOffer(rewardedRemainingToday)) {
            navigator.navigate(PaywallTeaserNavRoute(PaywallTeaserReason.CHAPTER_STUDY_LIMIT))
            return
        }
        val requestKey = target.toRewardedUnlockRequestKey()
        observeRewardedChapterStudyJob?.cancel()
        observeRewardedChapterStudyJob = viewModelScope.launch {
            studyUseCases.studyUnlockResultStore.observeEarned(requestKey).collect {
                openRewardedChapterStudy(target)
            }
        }
        navigator.navigate(
            StudyUnlockNavRoute(
                surface = StudyUnlockSurface.CHAPTER_STUDY,
                paywallSource = PaywallEntrySource.CHAPTER_STUDY,
                requestKey = requestKey,
                rewardedRemainingToday = rewardedRemainingToday,
            ),
        )
    }

    private fun openRewardedChapterStudy(target: ChapterStudyTargetModel) {
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_STUDY_GENERATION_STARTED,
            params = mapOf(
                AnalyticsParams.BOOK_ID to target.bookId.name,
                AnalyticsParams.CHAPTER_NUMBER to target.chapterNumber,
                AnalyticsParams.IS_PRO to false,
                AnalyticsParams.IS_REWARDED to true,
            ),
        )
        studyUseCases.chapterStudyGenerationCoordinator.start(
            target = target,
            isRewarded = true,
        )
        navigator.navigate(target.toChapterStudyNavRoute())
    }

    private fun ChapterStudyTargetModel.toChapterStudyNavRoute(): ChapterStudyNavRoute = ChapterStudyNavRoute(
        bookId = bookId.name,
        chapterNumber = chapterNumber,
        isCompanion = false,
    )

    private fun ChapterStudyTargetModel.toRewardedUnlockRequestKey(): String = listOf(
        REWARDED_UNLOCK_REQUEST_PREFIX,
        bookId.name,
        chapterNumber.toString(),
    ).joinToString(REQUEST_KEY_SEPARATOR)

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
            if (isEnabled) {
                resolveNextChapter()
                resolvePreviousChapter()
            } else {
                appendedChapters.update { emptyList() }
                prependedChapters.update { emptyList() }
                isAppendRequested.update { false }
                isPrependRequested.update { false }
                nextChapter.update { Loadable.Loading }
                previousChapter.update { Loadable.Loading }
            }
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
    ): List<ChapterLocationModel> = listOf(ChapterLocationModel(bookId = bookId, chapterNumber = route.chapterNumber)) +
        (prepended + appended).map { ChapterLocationModel(it.bookId, it.chapterNumber) }

    private fun isShowingChapter(chapter: ChapterRef): Boolean = ChapterLocationModel(
        bookId = chapter.bookId,
        chapterNumber = chapter.chapterNumber,
    ) in getShownChapters(
        prepended = prependedChapters.value,
        appended = appendedChapters.value,
    )

    override fun handleEvent(event: ReadUiEvent) {
        when (event) {
            ReadUiEvent.OnArrowBackClick -> navigator.navigateBack()

            ReadUiEvent.OnRetryClick -> retryCount.update { it + 1 }

            is ReadUiEvent.ToggleReadStatus -> toggleReadStatus(event)

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

            ReadUiEvent.OnDayCompletionBannerDismissed -> dayCompletionBanner.update { null }

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

    private fun toggleReadStatus(event: ReadUiEvent.ToggleReadStatus) {
        val key = ChapterLocationModel(bookId = event.bookId, chapterNumber = event.chapterNumber)
        val willBeRead = !isCurrentlyRead(key)
        pendingReadOverrides.update { it + (key to willBeRead) }
        val completedDay = dayCompletionCandidates.value[key]?.takeIf { willBeRead }
        if (completedDay != null) presentCompletedDay(completedDay)
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

    /*
     * Why: safety net for a tap that beat dayCompletionCandidates (cold screen, off-screen
     * chapter). The reader never knows which plan day opened it, so the day is looked up
     * from the chapter.
     */
    private suspend fun checkDayCompletion(
        bookId: BookId,
        chapterNumber: Int,
    ) {
        val day = getCompletedDayForChapter(
            bookId = bookId,
            chapterNumber = chapterNumber,
        ) ?: return
        presentCompletedDay(day)
    }

    /*
     * Why: settings not yet loaded fall back to the sheet rather than swallowing a
     * celebration the reader just earned.
     */
    private fun presentCompletedDay(day: PlanDayLocationModel) {
        val settings = studySuggestionSettings.value
        when {
            settings == null -> navigator.navigate(day.toDayReadingCompleteRoute())
            !settings.isEnabled -> Unit
            settings.mode == StudySuggestionMode.BANNER -> dayCompletionBanner.update { day }
            else -> navigator.navigate(day.toDayReadingCompleteRoute())
        }
    }

    private fun PlanDayLocationModel.toDayReadingCompleteRoute(): DayReadingCompleteNavRoute =
        DayReadingCompleteNavRoute(
            dayNumber = dayNumber,
            weekNumber = weekNumber,
            readingPlanType = readingPlanType.name,
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
                    isChapterRead = isWholeChapterRead(
                        chapterNumber = suggestion.chapterNumber,
                        bookId = suggestion.bookId,
                    ),
                    isFromBookDetails = route.isFromBookDetails,
                    targetVerseNumbers = emptyList(),
                ),
            )
        }
    }

    /*
     * Why: the request stands until the reading order resolves, so an end reached before the
     * lookup settles is served then instead of stalling until the next scroll.
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

    private fun prependPreviousChapter() {
        if (!uiState.value.settings.isVerticalReadingEnabled) return
        isPrependRequested.update { true }
        consumePreviousChapter()
    }

    private fun consumePreviousChapter() {
        val resolvedPreviousChapter = previousChapter.value
        if (resolvedPreviousChapter !is Loadable.Loaded || !isPrependRequested.value) return
        isPrependRequested.update { false }
        val chapter = resolvedPreviousChapter.value ?: return
        previousChapter.update { Loadable.Loading }
        prependedChapters.update { listOf(chapter) + it }
        viewModelScope.launch { resolvePreviousChapter() }
    }

    private suspend fun resolvePreviousChapter() {
        val firstChapter = prependedChapters.value.firstOrNull()
        previousChapter.update { Loadable.Loading }
        val previous = getPreviousChapter(
            bookId = firstChapter?.bookId ?: bookId,
            chapterNumber = firstChapter?.chapterNumber ?: route.chapterNumber,
            shouldForceCanonOrder = route.isFromBookDetails,
        )
        previousChapter.update { Loadable.Loaded(previous) }
        consumePreviousChapter()
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

    private fun ReadDataUiModel.toUiState(
        settings: ReaderSettingsModel,
        selection: VerseSelection?,
        overrides: Map<ChapterLocationModel, Boolean>,
        isLoadingPreviousChapter: Boolean,
        isLoadingNextChapter: Boolean,
    ): ReadUiState = ReadUiState(
        header = header.withReadOverride(overrides),
        content = content.withSelection(selection).withReadOverrides(overrides),
        settings = settings,
        isLoadingPreviousChapter = isLoadingPreviousChapter,
        isLoadingNextChapter = isLoadingNextChapter,
        dayCompletionBanner = null,
        verseFocus = null,
        isOpeningChapterStudy = false,
        isChapterStudyBeside = false,
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
            isNoteIconEnabled = true,
        ),
        isLoadingPreviousChapter = false,
        isLoadingNextChapter = false,
        dayCompletionBanner = null,
        verseFocus = null,
        isOpeningChapterStudy = false,
        isChapterStudyBeside = false,
    )

    private companion object {
        const val SOURCE_READER = "reader"
        const val SOURCE_RULER = "ruler"
        const val DIRECTION_PREVIOUS = "previous"
        const val DIRECTION_NEXT = "next"
        const val REWARDED_UNLOCK_REQUEST_PREFIX = "reader_chapter_study"
        const val REQUEST_KEY_SEPARATOR = "|"
    }
}
