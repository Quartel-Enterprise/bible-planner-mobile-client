package com.quare.bibleplanner.feature.read.presentation

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.copied_to_clipboard
import bibleplanner.feature.read.generated.resources.highlight_removed
import bibleplanner.feature.read.generated.resources.highlight_saved
import bibleplanner.feature.read.generated.resources.removed_from_saved
import bibleplanner.feature.read.generated.resources.verses_saved
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesShareContent
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterRead
import com.quare.bibleplanner.core.books.domain.usecase.ToggleWholeChapterReadStatus
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.loginnudge.domain.usecase.RequestLoginNudgeIfNeeded
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.DeleteHighlightColorNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestDownloadNotificationPermission
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.usecase.AddCustomHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ApplyHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveHighlightPalette
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ToggleSavedVerses
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.domain.usecase.ObserveReaderSettings
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFocusAid
import com.quare.bibleplanner.feature.read.presentation.factory.ObserveReadData
import com.quare.bibleplanner.feature.read.presentation.model.CustomColorUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadDataUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadInteractionUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadSelectionUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    private val observeHighlightPalette: ObserveHighlightPalette,
    private val applyHighlightColor: ApplyHighlightColor,
    private val addCustomHighlightColor: AddCustomHighlightColor,
    private val toggleSavedVerses: ToggleSavedVerses,
    private val getVersesShareContent: GetVersesShareContent,
    trackEvent: TrackEvent,
    val platform: Platform,
) : TrackedViewModel<ReadUiEvent>(trackEvent) {
    private val defaultCustomColor = CustomColorUiModel(
        hue = 265,
        lightness = 62,
    )
    private val bookId = BookId.valueOf(route.bookId)
    private val bookStringResource = bookId.toBookNameResource()

    private val interaction = MutableStateFlow(
        ReadInteractionUiModel(
            selectedBookId = null,
            selectedChapterNumber = null,
            selectedVerseNumbers = emptySet(),
            customColorPicker = null,
            isSidePanelOpen = true,
        ),
    )
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
        observeHighlightPalette(),
        interaction,
    ) { data, settings, customColors, interactionState ->
        data.toUiState(
            settings = settings,
            customColors = customColors,
            interactionState = interactionState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = loadingState(),
    )

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

            is ReadUiEvent.OnVerseClick -> toggleVerseSelection(event)

            ReadUiEvent.OnClearSelectionClick -> clearSelection()

            is ReadUiEvent.OnHighlightColorClick -> applyColor(event.color)

            ReadUiEvent.OnCustomColorPickerOpen ->
                interaction.update { it.copy(customColorPicker = defaultCustomColor) }

            is ReadUiEvent.OnCustomColorChange -> {
                interaction.update {
                    it.copy(
                        customColorPicker = CustomColorUiModel(
                            hue = event.hue,
                            lightness = event.lightness,
                        ),
                    )
                }
            }

            ReadUiEvent.OnCustomColorApplyClick -> applyCustomColor()

            ReadUiEvent.OnCustomColorCancelClick ->
                interaction.update { it.copy(customColorPicker = null) }

            is ReadUiEvent.OnCustomColorLongClick -> {
                emitAction(
                    ReadUiAction.NavigateToRoute(
                        route = DeleteHighlightColorNavRoute(colorKey = event.color.key),
                        replace = false,
                    ),
                )
            }

            ReadUiEvent.OnToggleSavedClick -> toggleSaved()

            ReadUiEvent.OnNoteClick -> openNote()

            ReadUiEvent.OnCopyClick -> copySelection()

            ReadUiEvent.OnShareClick -> shareSelection()

            ReadUiEvent.OnAppearanceClick -> {
                emitAction(
                    ReadUiAction.NavigateToRoute(
                        route = ReaderAppearanceNavRoute,
                        replace = false,
                    ),
                )
            }

            ReadUiEvent.OnSidePanelToggleClick -> toggleSidePanel()

            ReadUiEvent.OnRulerDismissClick -> dismissRuler()
        }
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

    private fun toggleVerseSelection(event: ReadUiEvent.OnVerseClick) {
        val current = interaction.value
        val isSameChapter = current.selectedBookId == event.bookId &&
            current.selectedChapterNumber == event.chapterNumber
        val previousSelection = if (isSameChapter) current.selectedVerseNumbers else emptySet()
        val isSelected = event.verseNumber !in previousSelection
        val selection = if (isSelected) {
            previousSelection + event.verseNumber
        } else {
            previousSelection - event.verseNumber
        }
        val hasSelection = selection.isNotEmpty()
        interaction.update { interactionState ->
            interactionState.copy(
                selectedBookId = if (hasSelection) event.bookId else null,
                selectedChapterNumber = if (hasSelection) event.chapterNumber else null,
                selectedVerseNumbers = selection,
                customColorPicker = null,
                isSidePanelOpen = interactionState.isSidePanelOpen || hasSelection,
            )
        }
        trackEvent(
            name = AnalyticsEventNames.VERSE_SELECTION_TOGGLED,
            params = mapOf(
                AnalyticsParams.IS_SELECTED to isSelected,
                AnalyticsParams.VERSE_COUNT to selection.size,
            ),
        )
    }

    private fun clearSelection() {
        interaction.update {
            it.copy(
                selectedBookId = null,
                selectedChapterNumber = null,
                selectedVerseNumbers = emptySet(),
                customColorPicker = null,
            )
        }
    }

    private fun applyColor(color: HighlightColor) {
        val refs = selectedRefs() ?: return
        viewModelScope.launch {
            val isApplied = applyHighlightColor(
                refs = refs,
                color = color,
            )
            trackEvent(
                name = if (isApplied) {
                    AnalyticsEventNames.VERSE_HIGHLIGHT_APPLIED
                } else {
                    AnalyticsEventNames.VERSE_HIGHLIGHT_REMOVED
                },
                params = mapOf(
                    AnalyticsParams.COLOR to color.key,
                    AnalyticsParams.IS_CUSTOM to (color is HighlightColor.Custom),
                    AnalyticsParams.VERSE_COUNT to refs.size,
                ),
            )
            _uiAction.emit(
                ReadUiAction.ShowMessage(
                    if (isApplied) Res.string.highlight_saved else Res.string.highlight_removed,
                ),
            )
        }
    }

    private fun applyCustomColor() {
        val picker = interaction.value.customColorPicker ?: return
        val color = HighlightColor.Custom(
            hue = picker.hue,
            lightness = picker.lightness,
        )
        interaction.update { it.copy(customColorPicker = null) }
        viewModelScope.launch {
            addCustomHighlightColor(color)
            trackEvent(
                name = AnalyticsEventNames.HIGHLIGHT_CUSTOM_COLOR_CREATED,
                params = mapOf(AnalyticsParams.COLOR to color.key),
            )
        }
        applyColor(color)
    }

    private fun toggleSaved() {
        val refs = selectedRefs() ?: return
        viewModelScope.launch {
            val isSaved = toggleSavedVerses(refs)
            trackEvent(
                name = AnalyticsEventNames.VERSE_SAVED_TOGGLED,
                params = mapOf(
                    AnalyticsParams.IS_SAVED to isSaved,
                    AnalyticsParams.VERSE_COUNT to refs.size,
                ),
            )
            _uiAction.emit(
                ReadUiAction.ShowMessage(
                    if (isSaved) Res.string.verses_saved else Res.string.removed_from_saved,
                ),
            )
        }
    }

    private fun openNote() {
        val selection = uiState.value.selection ?: return
        trackEvent(
            name = AnalyticsEventNames.VERSE_NOTE_OPENED,
            params = mapOf(
                AnalyticsParams.IS_EXISTING to (selection.noteId != null),
                AnalyticsParams.VERSE_COUNT to selection.verseNumbers.size,
            ),
        )
        emitAction(
            ReadUiAction.NavigateToRoute(
                route = VerseNoteNavRoute(
                    bookId = selection.bookId.name,
                    chapterNumber = selection.chapterNumber,
                    verseNumbers = selection.verseNumbers,
                    noteId = selection.noteId,
                ),
                replace = false,
            ),
        )
    }

    private fun copySelection() {
        val selection = uiState.value.selection ?: return
        viewModelScope.launch {
            val shareContent = getVersesShareContent(
                bookId = selection.bookId,
                chapterNumber = selection.chapterNumber,
                verseNumbers = selection.verseNumbers,
            ) ?: return@launch
            trackEvent(
                name = AnalyticsEventNames.VERSES_COPIED,
                params = mapOf(AnalyticsParams.VERSE_COUNT to selection.verseNumbers.size),
            )
            _uiAction.emit(
                ReadUiAction.CopyToClipboard(
                    "${shareContent.text}\n${shareContent.reference} (${shareContent.versionName})",
                ),
            )
            _uiAction.emit(ReadUiAction.ShowMessage(Res.string.copied_to_clipboard))
        }
    }

    private fun shareSelection() {
        val selection = uiState.value.selection ?: return
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_OPENED,
            params = mapOf(AnalyticsParams.VERSE_COUNT to selection.verseNumbers.size),
        )
        emitAction(
            ReadUiAction.NavigateToRoute(
                route = ShareVerseNavRoute(
                    bookId = selection.bookId.name,
                    chapterNumber = selection.chapterNumber,
                    verseNumbers = selection.verseNumbers,
                ),
                replace = false,
            ),
        )
    }

    private fun toggleSidePanel() {
        val isOpen = !interaction.value.isSidePanelOpen
        interaction.update { it.copy(isSidePanelOpen = isOpen) }
        trackEvent(
            name = AnalyticsEventNames.READ_SIDE_PANEL_TOGGLED,
            params = mapOf(AnalyticsParams.IS_OPEN to isOpen),
        )
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

    private fun selectedRefs(): List<VerseRef>? {
        val selection = uiState.value.selection ?: return null
        return selection.verseNumbers.map { verseNumber ->
            VerseRef(
                bookId = selection.bookId,
                chapterNumber = selection.chapterNumber,
                verseNumber = verseNumber,
            )
        }
    }

    private fun emitAction(action: ReadUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }

    private fun ReadDataUiModel.toUiState(
        settings: ReaderSettingsModel,
        customColors: List<HighlightColor.Custom>,
        interactionState: ReadInteractionUiModel,
    ): ReadUiState {
        val selectedChapter = (content as? ReadContentUiState.Success)
            ?.chapters
            ?.find {
                it.bookId == interactionState.selectedBookId &&
                    it.chapterNumber == interactionState.selectedChapterNumber
            }
        val selectedVerses = selectedChapter
            ?.verses
            ?.filter { it.number in interactionState.selectedVerseNumbers }
            .orEmpty()
        return ReadUiState(
            header = header,
            content = content.withSelection(selectedChapter, interactionState.selectedVerseNumbers),
            settings = settings,
            selection = selectedChapter
                ?.takeIf { selectedVerses.isNotEmpty() }
                ?.toSelectionUiModel(
                    selectedVerses = selectedVerses,
                    customColors = customColors,
                    customColorPicker = interactionState.customColorPicker,
                ),
            isSidePanelOpen = interactionState.isSidePanelOpen,
        )
    }

    private fun ReadContentUiState.withSelection(
        selectedChapter: ReadChapterUiModel?,
        selectedVerseNumbers: Set<Int>,
    ): ReadContentUiState = if (this !is ReadContentUiState.Success || selectedChapter == null) {
        this
    } else {
        copy(
            chapters = chapters.map { chapter ->
                if (chapter.chapterNumber != selectedChapter.chapterNumber ||
                    chapter.bookId != selectedChapter.bookId
                ) {
                    chapter
                } else {
                    chapter.copy(
                        verses = chapter.verses.map { verse ->
                            verse.copy(isSelected = verse.number in selectedVerseNumbers)
                        },
                    )
                }
            },
        )
    }

    private fun ReadChapterUiModel.toSelectionUiModel(
        selectedVerses: List<VerseUiModel>,
        customColors: List<HighlightColor.Custom>,
        customColorPicker: CustomColorUiModel?,
    ): ReadSelectionUiModel = ReadSelectionUiModel(
        bookId = bookId,
        bookStringResource = bookStringResource,
        chapterNumber = chapterNumber,
        verseNumbers = selectedVerses.map { it.number }.sorted(),
        customColors = customColors,
        activeColor = selectedVerses
            .map { it.highlightColor }
            .distinct()
            .singleOrNull(),
        isSelectionSaved = selectedVerses.all { it.isSaved },
        noteId = selectedVerses.firstNotNullOfOrNull { it.noteId },
        customColorPicker = customColorPicker,
    )

    private fun ReadNavigationSuggestionModel.toDirection(): String =
        if (this == uiState.value.header.navigationSuggestions.previous) DIRECTION_PREVIOUS else DIRECTION_NEXT

    private fun loadingState(): ReadUiState = ReadUiState(
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
        selection = null,
        isSidePanelOpen = true,
    )

    private companion object {
        const val SOURCE_READER = "reader"
        const val SOURCE_RULER = "ruler"
        const val DIRECTION_PREVIOUS = "previous"
        const val DIRECTION_NEXT = "next"
    }
}
