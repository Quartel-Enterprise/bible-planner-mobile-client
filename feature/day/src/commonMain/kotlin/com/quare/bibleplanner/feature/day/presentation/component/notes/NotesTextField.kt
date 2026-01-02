package com.quare.bibleplanner.feature.day.presentation.component.notes

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent

@Composable
internal fun NotesTextField(
    modifier: Modifier = Modifier,
    notesText: String,
    onEvent: (DayUiEvent) -> Unit,
    shouldClearFocus: Boolean = false,
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(shouldClearFocus) {
        if (shouldClearFocus) {
            focusManager.clearFocus()
        }
    }

    OutlinedTextField(
        value = notesText,
        onValueChange = { newValue ->
            onEvent(DayUiEvent.OnNotesChanged(newValue))
        },
        modifier = modifier
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onEvent(DayUiEvent.OnNotesFocus)
                }
            }.animateContentSize(
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
