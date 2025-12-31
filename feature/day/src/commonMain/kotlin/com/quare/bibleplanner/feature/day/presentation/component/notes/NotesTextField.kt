package com.quare.bibleplanner.feature.day.presentation.component.notes

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent

@Composable
internal fun NotesTextField(
    notesText: String,
    onEvent: (DayUiEvent) -> Unit,
) {
    OutlinedTextField(
        value = notesText,
        onValueChange = { newValue ->
            onEvent(DayUiEvent.OnNotesChanged(newValue))
        },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(300),
            ),
        placeholder = {
            NotesPlaceholder()
        },
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        minLines = 4,
    )
}
