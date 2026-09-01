package com.quare.bibleplanner.feature.verse.addnote.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesShareContent
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.verseannotations.domain.usecase.GetVerseNote
import com.quare.bibleplanner.core.verseannotations.domain.usecase.SaveVerseNote
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiEvent
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class VerseNoteViewModel(
    route: VerseNoteNavRoute,
    private val getVerseNote: GetVerseNote,
    private val saveVerseNote: SaveVerseNote,
    private val getVersesShareContent: GetVersesShareContent,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<VerseNoteUiEvent>(trackEvent) {
    private val noteId = route.noteId
    private val chapter = ChapterRef(
        bibleVersionId = route.bibleVersionId,
        bookId = BookId.valueOf(route.bookId),
        chapterNumber = route.chapterNumber,
    )
    private val verseNumbers = route.verseNumbers

    private val _uiState = MutableStateFlow(
        VerseNoteUiState(
            bookId = chapter.bookId,
            chapterNumber = chapter.chapterNumber,
            verseNumbers = verseNumbers,
            quote = "",
            text = "",
            isSaveEnabled = false,
        ),
    )
    val uiState: StateFlow<VerseNoteUiState> = _uiState

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
                navigator.navigateBack()
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
                bookId = chapter.bookId,
                chapterNumber = chapter.chapterNumber,
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
                chapter = chapter,
                verseNumbers = verseNumbers,
                text = text,
            )
            navigator.navigateBack()
        }
    }
}
