package com.quare.bibleplanner.feature.day.presentation.component.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        NotesTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp),
            notesText = notesText,
            onValueChange = { newValue ->
                onEvent(DayUiEvent.OnNotesChanged(newValue))
            },
        )
    }
}
