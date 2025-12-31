package com.quare.bibleplanner.feature.day.presentation.component.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent

@Composable
internal fun NotesSection(
    modifier: Modifier = Modifier,
    notesText: String,
    onEvent: (DayUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        NotesHeaderSection(
            onDeleteClick = { onEvent(DayUiEvent.OnNotesClear) },
        )
        NotesTextField(notesText, onEvent)
    }
}
