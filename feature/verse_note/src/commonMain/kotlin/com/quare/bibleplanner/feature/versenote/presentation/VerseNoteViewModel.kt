package com.quare.bibleplanner.feature.versenote.presentation

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.verse_note.generated.resources.Res
import bibleplanner.feature.verse_note.generated.resources.note_saved
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesShareContent
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.verseannotations.domain.usecase.GetVerseNote
import com.quare.bibleplanner.core.verseannotations.domain.usecase.SaveVerseNote
import com.quare.bibleplanner.feature.versenote.presentation.model.VerseNoteUiAction
import com.quare.bibleplanner.feature.versenote.presentation.model.VerseNoteUiEvent
import com.quare.bibleplanner.feature.versenote.presentation.model.VerseNoteUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class VerseNoteViewModel(
    route: VerseNoteNavRoute,
    private val getVerseNote: GetVerseNote,
    private val saveVerseNote: SaveVerseNote,
    private val getVersesShareContent: GetVersesShareContent,
    trackEvent: TrackEvent,
) : TrackedViewModel<VerseNoteUiEvent>(trackEvent) {
    private val noteId = route.noteId
    private val bookId = BookId.valueOf(route.bookId)
    private val chapterNumber = route.chapterNumber
    private val verseNumbers = route.verseNumbers

    private val _uiState = MutableStateFlow(
        VerseNoteUiState(
            bookId = bookId,
            chapterNumber = chapterNumber,
            verseNumbers = verseNumbers,
            quote = "",
            text = "",
            isSaveEnabled = false,
        ),
    )
    val uiState: StateFlow<VerseNoteUiState> = _uiState

    private val _uiAction = MutableSharedFlow<VerseNoteUiAction>()
    val uiAction: SharedFlow<VerseNoteUiAction> = _uiAction

    init {
        loadNote()
        loadQuote()
    }

    override fun handleEvent(event: VerseNoteUiEvent) {
        when (event) {
            is VerseNoteUiEvent.OnTextChange -> {
                _uiState.update {
                    it.copy(
                        text = event.text,
                        isSaveEnabled = event.text.isNotBlank(),
                    )
                }
            }

            VerseNoteUiEvent.OnSaveClick -> saveNote()

            VerseNoteUiEvent.OnDismiss ->
                viewModelScope.launch { _uiAction.emit(VerseNoteUiAction.NavigateBack) }
        }
    }

    private fun loadNote() {
        val existingNoteId = noteId ?: return
        viewModelScope.launch {
            val note = getVerseNote(existingNoteId) ?: return@launch
            _uiState.update {
                it.copy(
                    text = note.text,
                    isSaveEnabled = note.text.isNotBlank(),
                )
            }
        }
    }

    private fun loadQuote() {
        viewModelScope.launch {
            val shareContent = getVersesShareContent(
                bookId = bookId,
                chapterNumber = chapterNumber,
                verseNumbers = verseNumbers,
            ) ?: return@launch
            _uiState.update { it.copy(quote = shareContent.text) }
        }
    }

    private fun saveNote() {
        val text = uiState.value.text
        trackEvent(
            name = AnalyticsEventNames.VERSE_NOTE_SAVED,
            params = mapOf(
                AnalyticsParams.NOTE_LENGTH to text.length,
                AnalyticsParams.VERSE_COUNT to verseNumbers.size,
                AnalyticsParams.IS_EXISTING to (noteId != null),
            ),
        )
        viewModelScope.launch {
            saveVerseNote(
                noteId = noteId,
                bookId = bookId,
                chapterNumber = chapterNumber,
                verseNumbers = verseNumbers,
                text = text,
            )
            _uiAction.emit(VerseNoteUiAction.NavigateBackWithMessage(Res.string.note_saved))
        }
    }
}
