package com.quare.bibleplanner.feature.addnotesfreewarning.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import bibleplanner.feature.add_notes_free_warning.generated.resources.Res
import bibleplanner.feature.add_notes_free_warning.generated.resources.add_notes_free_warning_message
import bibleplanner.feature.add_notes_free_warning.generated.resources.add_notes_free_warning_title
import bibleplanner.feature.add_notes_free_warning.generated.resources.cancel
import bibleplanner.feature.add_notes_free_warning.generated.resources.see_plans
import com.quare.bibleplanner.feature.addnotesfreewarning.presentation.model.AddNotesFreeWarningUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddNotesFreeWarningDialog(
    maxFreeNotesAmount: Int,
    onEvent: (AddNotesFreeWarningUiEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onEvent(AddNotesFreeWarningUiEvent.OnCancel)
        },
        title = {
            Text(
                text = stringResource(Res.string.add_notes_free_warning_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(
                        Res.string.add_notes_free_warning_message,
                        maxFreeNotesAmount,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onEvent(AddNotesFreeWarningUiEvent.OnSubscribeToPremium)
                },
            ) {
                Text(
                    text = stringResource(Res.string.see_plans),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onEvent(AddNotesFreeWarningUiEvent.OnCancel)
                },
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}
