package com.quare.bibleplanner.android.feature.day

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.day.presentation.model.DayUiEvent

private const val NOTES_MIN_LINES = 4
private const val NOTES_RESIZE_MILLIS = 300

@Composable
internal fun DayNotesSection(
    notes: String,
    onEvent: (DayUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.day_notes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(onClick = { onEvent(DayUiEvent.OnNotesClear) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.day_clear_notes),
                )
            }
        }
        DayNotesField(
            notes = notes,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun DayNotesField(
    notes: String,
    onEvent: (DayUiEvent) -> Unit,
) {
    val fieldState = rememberTextFieldState(initialText = notes)
    val sync = remember { NotesSync(lastSent = notes) }
    NotesSyncEffect(
        notes = notes,
        fieldState = fieldState,
        sync = sync,
        onEvent = onEvent,
    )
    OutlinedTextField(
        state = fieldState,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onEvent(DayUiEvent.OnNotesFocus)
                }
            }.animateContentSize(animationSpec = tween(NOTES_RESIZE_MILLIS)),
        placeholder = {
            Text(
                text = stringResource(R.string.day_notes_placeholder),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = NOTES_MIN_LINES),
    )
}

/*
 * Why: the field keeps the text the user types and sends every change, so a state that arrives a frame late
 * never overwrites newer keystrokes. Notes the ViewModel echoes back are ignored; any other value (deleted from
 * the dialog, or removed by the free notes limit) replaces the field's text.
 */
private class NotesSync(
    var lastSent: String,
) {
    val pendingEchoes: ArrayDeque<String> = ArrayDeque()
}

@Composable
private fun NotesSyncEffect(
    notes: String,
    fieldState: TextFieldState,
    sync: NotesSync,
    onEvent: (DayUiEvent) -> Unit,
) {
    LaunchedEffect(fieldState, sync) {
        snapshotFlow { fieldState.text.toString() }.collect { text ->
            if (text != sync.lastSent) {
                sync.lastSent = text
                sync.pendingEchoes.addLast(text)
                onEvent(DayUiEvent.OnNotesChanged(text))
            }
        }
    }
    LaunchedEffect(notes) {
        val echoIndex = sync.pendingEchoes.indexOf(notes)
        if (echoIndex >= 0) {
            repeat(echoIndex) { sync.pendingEchoes.removeFirst() }
        } else if (fieldState.text.toString() != notes) {
            sync.pendingEchoes.clear()
            sync.lastSent = notes
            fieldState.setTextAndPlaceCursorAtEnd(notes)
        }
    }
}
