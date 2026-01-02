package com.quare.bibleplanner.feature.deletenotes.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import bibleplanner.feature.delete_notes.generated.resources.Res
import bibleplanner.feature.delete_notes.generated.resources.cancel
import bibleplanner.feature.delete_notes.generated.resources.delete
import bibleplanner.feature.delete_notes.generated.resources.delete_notes_message
import bibleplanner.feature.delete_notes.generated.resources.delete_notes_title
import com.quare.bibleplanner.feature.deletenotes.presentation.model.DeleteNotesUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteNotesDialog(onEvent: (DeleteNotesUiEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = {
            onEvent(DeleteNotesUiEvent.OnCancel)
        },
        title = {
            Text(
                text = stringResource(Res.string.delete_notes_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.delete_notes_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onEvent(DeleteNotesUiEvent.OnConfirmDelete)
                },
            ) {
                Text(
                    text = stringResource(Res.string.delete),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onEvent(DeleteNotesUiEvent.OnCancel)
                },
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}
