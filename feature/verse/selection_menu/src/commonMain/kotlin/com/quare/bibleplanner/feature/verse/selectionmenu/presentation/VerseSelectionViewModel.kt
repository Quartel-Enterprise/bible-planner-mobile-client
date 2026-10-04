package com.quare.bibleplanner.feature.verse.selectionmenu.presentation

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.verse.selection_menu.generated.resources.Res
import bibleplanner.feature.verse.selection_menu.generated.resources.copied_to_clipboard
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesShareContent
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningType
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
import com.quare.bibleplanner.core.verseannotations.domain.usecase.GetMaxFreeVerseNotesAmount
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveChapterAnnotations
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveHighlightPalette
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveVerseSelection
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ShouldBlockAddVerseNote
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ToggleSavedVerses
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.CustomColorUiModel
import com.quare.bibleplanner.feature.verse.selectionmenu.presentation.model.SelectionNoteUiModel
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class VerseSelectionViewModel(
    private val observeVerseSelection: ObserveVerseSelection,
    private val clearVerseSelection: ClearVerseSelection,
    private val applyHighlightColor: ApplyHighlightColor,
    private val addCustomHighlightColor: AddCustomHighlightColor,
    private val toggleSavedVerses: ToggleSavedVerses,
    private val getVersesShareContent: GetVersesShareContent,
    private val shouldBlockAddVerseNote: ShouldBlockAddVerseNote,
    private val getMaxFreeVerseNotesAmount: GetMaxFreeVerseNotesAmount,
    val platform: Platform,
    private val navigator: Navigator,
    observeChapterAnnotations: ObserveChapterAnnotations,
    observeHighlightPalette: ObserveHighlightPalette,
    observeIsProUser: ObserveIsProUser,
    trackEvent: TrackEvent,
) : TrackedViewModel<VerseSelectionUiEvent>(trackEvent) {
    private val defaultCustomColor = CustomColorUiModel(
        hue = 265,
        lightness = 62,
    )
    private val customColorPicker = MutableStateFlow<CustomColorUiModel?>(null)

    /*
     * Why: the panel opens on the selection alone instead of waiting for Room, which
     * delayed the sheet noticeably; annotations fill in when they land.
     */
    private val emptyAnnotations = ChapterAnnotations(
        highlightColorByVerse = emptyMap(),
        savedVerseNumbers = emptySet(),
        noteIdByVerse = emptyMap(),
        noteVerseNumbersById = emptyMap(),
    )

    val uiAction: SharedFlow<VerseSelectionUiAction>
        field = MutableSharedFlow<VerseSelectionUiAction>()

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
                note = selection.verseNumbers
                    .firstNotNullOfOrNull { annotations.noteIdByVerse[it] }
                    ?.let { noteId -> annotations.toSelectionNote(noteId) },
                customColorPicker = picker,
                isProUser = isPro,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    /*
     * Why: system back drops this entry without the close button, so the selection is
     * cleared here. Never close by watching the selection empty: that back would also
     * pop the reader.
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

            is VerseSelectionUiEvent.OnCustomColorLongClick -> navigator.navigate(
                DeleteHighlightColorNavRoute(colorKey = event.color.key),
            )

            VerseSelectionUiEvent.OnToggleSavedClick -> toggleSaved()

            VerseSelectionUiEvent.OnNoteClick -> openNote()

            VerseSelectionUiEvent.OnCopyClick -> copySelection()

            VerseSelectionUiEvent.OnShareClick -> shareSelection()
        }
    }

    private fun close() {
        clearVerseSelection()
        navigator.navigateBack()
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
        navigator.navigate(PaywallTeaserNavRoute(PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR))
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

    /*
     * Why: a selection that touches a note opens that note over its own verses, so viewing it
     * never rewrites the passage it was written for.
     */
    private fun openNote() {
        val selection = getCurrentSelection() ?: return
        val note = uiState.value?.note
        val verseNumbers = note?.verseNumbers ?: selection.verseNumbers
        trackEvent(
            name = AnalyticsEventNames.VERSE_NOTE_OPENED,
            params = mapOf(
                AnalyticsParams.IS_EXISTING to (note != null),
                AnalyticsParams.VERSE_COUNT to verseNumbers.size,
            ),
        )
        viewModelScope.launch {
            if (note == null && shouldBlockAddVerseNote()) {
                blockAddVerseNote()
            } else {
                navigator.navigate(
                    VerseNoteNavRoute(
                        bibleVersionId = selection.chapter.bibleVersionId,
                        bookId = selection.chapter.bookId.name,
                        chapterNumber = selection.chapter.chapterNumber,
                        verseNumbers = verseNumbers,
                        noteId = note?.noteId,
                    ),
                )
            }
        }
    }

    private suspend fun blockAddVerseNote() {
        val maxFreeVerseNotes = getMaxFreeVerseNotesAmount()
        trackEvent(
            name = AnalyticsEventNames.VERSE_NOTES_LIMIT_REACHED,
            params = mapOf(
                AnalyticsParams.MAX_FREE_NOTES to maxFreeVerseNotes,
                AnalyticsParams.SOURCE to VERSE_NOTES_LIMIT_SOURCE,
            ),
        )
        navigator.navigate(
            AddNotesFreeWarningNavRoute(
                maxFreeNotesAmount = maxFreeVerseNotes,
                type = AddNotesFreeWarningType.VERSE,
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
            uiAction.emit(
                VerseSelectionUiAction.CopyToClipboard(shareContent.shareText),
            )
            uiAction.emit(VerseSelectionUiAction.ShowMessage(Res.string.copied_to_clipboard))
        }
    }

    private fun shareSelection() {
        val selection = getCurrentSelection() ?: return
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_OPENED,
            params = mapOf(AnalyticsParams.VERSE_COUNT to selection.verseNumbers.size),
        )
        navigator.navigate(
            ShareVerseNavRoute(
                bookId = selection.chapter.bookId.name,
                chapterNumber = selection.chapter.chapterNumber,
                verseNumbers = selection.verseNumbers,
            ),
        )
    }

    private fun ChapterAnnotations.toSelectionNote(noteId: String): SelectionNoteUiModel = SelectionNoteUiModel(
        noteId = noteId,
        verseNumbers = noteVerseNumbersById.getValue(noteId),
    )

    private fun getCurrentSelection(): VerseSelection? = observeVerseSelection().value

    private fun emitAction(action: VerseSelectionUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }

    companion object {
        private const val VERSE_NOTES_LIMIT_SOURCE = "selection_menu"
    }
}
