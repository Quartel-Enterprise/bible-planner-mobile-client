package com.quare.bibleplanner.feature.read.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterRead
import com.quare.bibleplanner.core.books.domain.usecase.ToggleWholeChapterReadStatus
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
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
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
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
    private val requestLoginNudgeIfNeeded: RequestLoginNudgeIfNeeded,
    private val downloaderFacade: BibleVersionDownloaderFacade,
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlow,
    private val requestDownloadNotificationPermission: RequestDownloadNotificationPermission,
    private val observeReaderSettings: ObserveReaderSettings,
    private val setReaderFocusAid: SetReaderFocusAid,
    private val observeVerseSelection: ObserveVerseSelection,
    private val toggleVerseSelection: ToggleVerseSelection,
    private val clearVerseSelection: ClearVerseSelection,
    trackEvent: TrackEvent,
    val platform: Platform,
) : TrackedViewModel<ReadUiEvent>(trackEvent) {
    private val bookId = BookId.valueOf(route.bookId)
    private val bookStringResource = bookId.toBookNameResource()
    private val retryCount = MutableStateFlow(0)

    private val _uiAction = MutableSharedFlow<ReadUiAction>()
    val uiAction: SharedFlow<ReadUiAction> = _uiAction

    private val dataFlow = combine(
        observeReaderSettings().map { it.isVerticalReadingEnabled }.distinctUntilChanged(),
        retryCount,
    ) { isVerticalReadingEnabled, _ -> isVerticalReadingEnabled }
        .flatMapLatest { isVerticalReadingEnabled ->
            observeReadData(
                bookId = bookId,
                chapterNumber = route.chapterNumber,
                bookStringResource = bookStringResource,
                isInitiallyRead = route.isChapterRead,
                isFromBookDetails = route.isFromBookDetails,
                isVerticalReadingEnabled = isVerticalReadingEnabled,
            )
        }

    val uiState: StateFlow<ReadUiState> = combine(
        dataFlow,
        observeReaderSettings(),
        observeVerseSelection(),
    ) { data, settings, selection ->
        data.toUiState(
            settings = settings,
            selection = selection,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = createLoadingState(),
    )

    init {
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
        viewModelScope.launch {
            val isRead = toggleWholeChapterReadStatus(
                bookId = event.bookId,
                chapterNumber = event.chapterNumber,
            )
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
        }
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
    ): ReadUiState = ReadUiState(
        header = header,
        content = content.withSelection(selection),
        settings = settings,
    )

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
            versionAbbreviation = "",
        ),
        content = ReadContentUiState.Loading,
        settings = ReaderSettingsModel(
            fontSizeSp = ReaderFontSize.DEFAULT,
            font = ReaderFont.LORA,
            isRulerEnabled = false,
            isFocusedVerseEnabled = false,
            isVerticalReadingEnabled = false,
        ),
    )

    private companion object {
        const val SOURCE_READER = "reader"
        const val SOURCE_RULER = "ruler"
        const val DIRECTION_PREVIOUS = "previous"
        const val DIRECTION_NEXT = "next"
    }
}
