package com.quare.bibleplanner.feature.deleteversion.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import bibleplanner.feature.delete_version.generated.resources.Res
import bibleplanner.feature.delete_version.generated.resources.cancel
import bibleplanner.feature.delete_version.generated.resources.delete
import bibleplanner.feature.delete_version.generated.resources.delete_version_message
import bibleplanner.feature.delete_version.generated.resources.delete_version_title
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiEvent
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeleteVersionScreen(
    uiState: DeleteVersionUiState,
    onEvent: (DeleteVersionUiEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onEvent(DeleteVersionUiEvent.OnCancel)
        },
        title = {
            Text(
                text = stringResource(Res.string.delete_version_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.delete_version_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
            }
        },
        confirmButton = {
            when (uiState) {
                is DeleteVersionUiState.Loading -> CircularProgressIndicator()

                DeleteVersionUiState.Idle -> TextButton(
                    onClick = {
                        onEvent(DeleteVersionUiEvent.OnConfirmDelete)
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.delete),
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onEvent(DeleteVersionUiEvent.OnCancel)
                },
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}
