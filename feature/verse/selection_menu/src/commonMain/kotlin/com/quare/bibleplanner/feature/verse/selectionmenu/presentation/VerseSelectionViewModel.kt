package com.quare.bibleplanner.feature.verse.selectionmenu.presentation

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.verse.selection_menu.generated.resources.Res
import bibleplanner.feature.verse.selection_menu.generated.resources.copied_to_clipboard
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesShareContent
import com.quare.bibleplanner.core.model.route.DeleteHighlightColorNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.AddCustomHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ApplyHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ClearVerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveChapterAnnotations
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveHighlightPalette
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveVerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ToggleSavedVerses
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.CustomColorUiModel
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiAction
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiEvent
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.VerseSelectionUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns everything that can be done to the verses the reader picked. The selection itself is not its
 * own: it comes from the shared store the reader writes to, so tapping another verse updates this
 * panel without navigating.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class VerseSelectionViewModel(
    private val observeVerseSelection: ObserveVerseSelection,
    private val clearVerseSelection: ClearVerseSelection,
    private val observeChapterAnnotations: ObserveChapterAnnotations,
    private val observeHighlightPalette: ObserveHighlightPalette,
    private val applyHighlightColor: ApplyHighlightColor,
    private val addCustomHighlightColor: AddCustomHighlightColor,
    private val toggleSavedVerses: ToggleSavedVerses,
    private val getVersesShareContent: GetVersesShareContent,
    private val observeIsProUser: ObserveIsProUser,
    val platform: Platform,
    trackEvent: TrackEvent,
) : TrackedViewModel<VerseSelectionUiEvent>(trackEvent) {
    private val defaultCustomColor = CustomColorUiModel(
        hue = 265,
        lightness = 62,
    )
    private val customColorPicker = MutableStateFlow<CustomColorUiModel?>(null)

    /**
     * What the panel shows before the database answers. Both the annotations and the palette come
     * from Room, and waiting for them would hold the whole panel back by a query or two — long
     * enough to feel like a delay between tapping a verse and the sheet arriving. It opens on the
     * selection alone and fills in the colour, the bookmark and the note when they land.
     */
    private val emptyAnnotations = ChapterAnnotations(
        highlightColorByVerse = emptyMap(),
        savedVerseNumbers = emptySet(),
        noteIdByVerse = emptyMap(),
    )

    private val _uiAction = MutableSharedFlow<VerseSelectionUiAction>()
    val uiAction: SharedFlow<VerseSelectionUiAction> = _uiAction

    val uiState: StateFlow<VerseSelectionUiState?> = combine(
        observeVerseSelection()
            .flatMapLatest { selection ->
                selection
                    ?.let { safeSelection ->
                        observeChapterAnnotations(safeSelection.chapter)
                    } ?: flowOf(emptyAnnotations)
            }.onStart { emit(emptyAnnotations) },
        observeVerseSelection(),
        observeHighlightPalette().onStart { emit(emptyList()) },
        customColorPicker,
        observeIsProUser().onStart { emit(false) },
    ) { annotations, selection, customColors, picker, isPro ->
        if (selection == null) {
            null
        } else {
            VerseSelectionUiState(
                chapter = selection.chapter,
                verseNumbers = selection.verseNumbers,
                customColors = customColors,
                activeColor = selection.verseNumbers
                    .map { annotations.highlightColorByVerse[it] }
                    .distinct()
                    .singleOrNull(),
                isSelectionSaved = selection.verseNumbers.all { it in annotations.savedVerseNumbers },
                noteId = selection.verseNumbers.firstNotNullOfOrNull { annotations.noteIdByVerse[it] },
                customColorPicker = picker,
                isProUser = isPro,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    /**
     * Covers the system back, which drops this entry without going through the close button: the
     * verses must not stay marked with no panel to act on them.
     *
     * Closing is never driven by watching the selection empty — that would fire here too, and the
     * resulting back would pop the reader along with this entry. Both ways of closing say so
     * explicitly instead.
     */
    override fun onCleared() {
        clearVerseSelection()
        super.onCleared()
    }

    override fun handleEvent(event: VerseSelectionUiEvent) {
        when (event) {
            VerseSelectionUiEvent.OnClearSelectionClick -> close()

            is VerseSelectionUiEvent.OnHighlightColorClick -> applyColor(event.color)

            VerseSelectionUiEvent.OnCustomColorPickerOpen -> openCustomColorPicker()

            VerseSelectionUiEvent.OnLockedColorClick -> openColorPaywall()

            is VerseSelectionUiEvent.OnCustomColorChange -> {
                customColorPicker.update {
                    CustomColorUiModel(
                        hue = event.hue,
                        lightness = event.lightness,
                    )
                }
            }

            VerseSelectionUiEvent.OnCustomColorApplyClick -> applyCustomColor()

            VerseSelectionUiEvent.OnCustomColorCancelClick -> customColorPicker.update { null }

            is VerseSelectionUiEvent.OnCustomColorLongClick -> {
                emitAction(
                    VerseSelectionUiAction.NavigateToRoute(
                        DeleteHighlightColorNavRoute(colorKey = event.color.key),
                    ),
                )
            }

            VerseSelectionUiEvent.OnToggleSavedClick -> toggleSaved()

            VerseSelectionUiEvent.OnNoteClick -> openNote()

            VerseSelectionUiEvent.OnCopyClick -> copySelection()

            VerseSelectionUiEvent.OnShareClick -> shareSelection()
        }
    }

    private fun close() {
        clearVerseSelection()
        emitAction(VerseSelectionUiAction.NavigateBack)
    }

    private fun openCustomColorPicker() {
        if (uiState.value?.isProUser == false) {
            openColorPaywall()
            return
        }
        trackEvent(
            name = AnalyticsEventNames.HIGHLIGHT_COLOR_PICKER_OPENED,
            params = emptyMap(),
        )
        customColorPicker.update { defaultCustomColor }
    }

    private fun openColorPaywall() {
        trackEvent(
            name = AnalyticsEventNames.HIGHLIGHT_CUSTOM_COLOR_LOCKED_CLICKED,
            params = emptyMap(),
        )
        emitAction(
            VerseSelectionUiAction.NavigateToRoute(
                PaywallTeaserNavRoute(PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR),
            ),
        )
    }

    private fun applyColor(color: HighlightColor) {
        val selection = getCurrentSelection() ?: return
        viewModelScope.launch {
            val isApplied = applyHighlightColor(
                refs = selection.refs,
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
                    AnalyticsParams.VERSE_COUNT to selection.verseNumbers.size,
                ),
            )
        }
    }

    private fun applyCustomColor() {
        val picker = customColorPicker.value ?: return
        val color = HighlightColor.Custom(
            hue = picker.hue,
            lightness = picker.lightness,
        )
        customColorPicker.update { null }
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
        val selection = getCurrentSelection() ?: return
        viewModelScope.launch {
            val isSaved = toggleSavedVerses(selection.refs)
            trackEvent(
                name = AnalyticsEventNames.VERSE_SAVED_TOGGLED,
                params = mapOf(
                    AnalyticsParams.IS_SAVED to isSaved,
                    AnalyticsParams.VERSE_COUNT to selection.verseNumbers.size,
                ),
            )
        }
    }

    private fun openNote() {
        val selection = getCurrentSelection() ?: return
        val noteId = uiState.value?.noteId
        trackEvent(
            name = AnalyticsEventNames.VERSE_NOTE_OPENED,
            params = mapOf(
                AnalyticsParams.IS_EXISTING to (noteId != null),
                AnalyticsParams.VERSE_COUNT to selection.verseNumbers.size,
            ),
        )
        emitAction(
            VerseSelectionUiAction.NavigateToRoute(
                VerseNoteNavRoute(
                    bibleVersionId = selection.chapter.bibleVersionId,
                    bookId = selection.chapter.bookId.name,
                    chapterNumber = selection.chapter.chapterNumber,
                    verseNumbers = selection.verseNumbers,
                    noteId = noteId,
                ),
            ),
        )
    }

    private fun copySelection() {
        val selection = getCurrentSelection() ?: return
        viewModelScope.launch {
            val shareContent = getVersesShareContent(
                bookId = selection.chapter.bookId,
                chapterNumber = selection.chapter.chapterNumber,
                verseNumbers = selection.verseNumbers,
            ) ?: return@launch
            trackEvent(
                name = AnalyticsEventNames.VERSES_COPIED,
                params = mapOf(AnalyticsParams.VERSE_COUNT to selection.verseNumbers.size),
            )
            _uiAction.emit(
                VerseSelectionUiAction.CopyToClipboard(shareContent.shareText),
            )
            _uiAction.emit(VerseSelectionUiAction.ShowMessage(Res.string.copied_to_clipboard))
        }
    }

    private fun shareSelection() {
        val selection = getCurrentSelection() ?: return
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_OPENED,
            params = mapOf(AnalyticsParams.VERSE_COUNT to selection.verseNumbers.size),
        )
        emitAction(
            VerseSelectionUiAction.NavigateToRoute(
                ShareVerseNavRoute(
                    bookId = selection.chapter.bookId.name,
                    chapterNumber = selection.chapter.chapterNumber,
                    verseNumbers = selection.verseNumbers,
                ),
            ),
        )
    }

    private fun getCurrentSelection(): VerseSelection? = observeVerseSelection().value

    private fun emitAction(action: VerseSelectionUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }
}
