package com.quare.bibleplanner.feature.deleteprogress.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import bibleplanner.feature.delete_progress.generated.resources.Res
import bibleplanner.feature.delete_progress.generated.resources.cancel
import bibleplanner.feature.delete_progress.generated.resources.delete
import bibleplanner.feature.delete_progress.generated.resources.delete_all_progress_message
import bibleplanner.feature.delete_progress.generated.resources.delete_all_progress_title
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteAllProgressScreen(onEvent: (DeleteAllProgressUiEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = {
            onEvent(DeleteAllProgressUiEvent.OnCancel)
        },
        title = {
            Text(
                text = stringResource(Res.string.delete_all_progress_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.delete_all_progress_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onEvent(DeleteAllProgressUiEvent.OnConfirmDelete)
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
                    onEvent(DeleteAllProgressUiEvent.OnCancel)
                },
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}
