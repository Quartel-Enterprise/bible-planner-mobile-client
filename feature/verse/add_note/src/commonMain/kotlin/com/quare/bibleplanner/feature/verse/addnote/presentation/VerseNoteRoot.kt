package com.quare.bibleplanner.feature.verse.addnote.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import bibleplanner.feature.verse.add_note.generated.resources.Res
import bibleplanner.feature.verse.add_note.generated.resources.verse_note_title
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.feature.verse.addnote.presentation.model.VerseNoteUiEvent
import com.quare.bibleplanner.ui.component.ResponsiveDialogSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.verseNote() {
    entry<VerseNoteNavRoute>(
        metadata = DialogSceneStrategy.dialog(DialogProperties(usePlatformDefaultWidth = false)),
    ) { route ->
        val viewModel = koinViewModel<VerseNoteViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsState()
        val onEvent = viewModel::onEvent
        ResponsiveDialogSheet(
            onCloseClick = { onEvent(VerseNoteUiEvent.OnDismiss) },
            title = stringResource(Res.string.verse_note_title),
            sheetBottomBreathingRoom = 12.dp,
        ) {
            VerseNoteContent(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}
