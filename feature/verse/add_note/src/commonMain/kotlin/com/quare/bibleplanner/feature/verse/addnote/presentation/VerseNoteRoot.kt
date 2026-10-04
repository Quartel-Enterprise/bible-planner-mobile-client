package com.quare.bibleplanner.feature.verse.addnote.presentation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.verse.add_note.generated.resources.Res
import bibleplanner.feature.verse.add_note.generated.resources.edit_verse_note_title
import bibleplanner.feature.verse.add_note.generated.resources.verse_note_title
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiEvent
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.verseNote() {
    entry<VerseNoteNavRoute>(metadata = getSheetPane()) { route ->
        val viewModel = koinViewModel<VerseNoteViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsState()
        val onEvent = viewModel::onEvent
        ResponsiveDialogSheet(
            onCloseClick = { onEvent(VerseNoteUiEvent.OnDismiss) },
            title = stringResource(
                if (uiState.isExisting) Res.string.edit_verse_note_title else Res.string.verse_note_title,
            ),
            sheetBottomBreathingRoom = 12.dp,
        ) {
            VerseNoteContent(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}
