package com.quare.bibleplanner.feature.deleteprogress.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import bibleplanner.feature.delete_progress.generated.resources.Res
import bibleplanner.feature.delete_progress.generated.resources.cancel
import bibleplanner.feature.delete_progress.generated.resources.delete
import bibleplanner.feature.delete_progress.generated.resources.delete_all_progress_deleted
import bibleplanner.feature.delete_progress.generated.resources.delete_all_progress_deleting
import bibleplanner.feature.delete_progress.generated.resources.delete_all_progress_keeps_account
import bibleplanner.feature.delete_progress.generated.resources.delete_all_progress_message
import bibleplanner.feature.delete_progress.generated.resources.delete_all_progress_removes_chapters
import bibleplanner.feature.delete_progress.generated.resources.delete_all_progress_removes_plans
import bibleplanner.feature.delete_progress.generated.resources.delete_all_progress_title
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiEvent
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiState
import com.quare.bibleplanner.ui.component.dialog.DialogConsequenceRow
import com.quare.bibleplanner.ui.component.dialog.DialogConsequenceType
import org.jetbrains.compose.resources.stringResource

private val consequenceSpacing = 10.dp
private val progressIconSize = 44.dp
private val progressSpacing = 18.dp

@Composable
internal fun DeleteAllProgressScreen(
    uiState: DeleteAllProgressUiState,
    onEvent: (DeleteAllProgressUiEvent) -> Unit,
) {
    val isIdle = uiState == DeleteAllProgressUiState.Idle
    AlertDialog(
        onDismissRequest = {
            onEvent(DeleteAllProgressUiEvent.OnCancel)
        },
        properties = DialogProperties(
            dismissOnBackPress = isIdle,
            dismissOnClickOutside = isIdle,
        ),
        icon = if (isIdle) {
            {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            null
        },
        title = if (isIdle) {
            {
                Text(
                    text = stringResource(Res.string.delete_all_progress_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            null
        },
        text = {
            if (isIdle) {
                DeleteAllProgressConsequences()
            } else {
                DeleteAllProgressStatus(uiState = uiState)
            }
        },
        confirmButton = {
            if (isIdle) {
                TextButton(
                    onClick = {
                        onEvent(DeleteAllProgressUiEvent.OnConfirmDelete)
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        dismissButton = {
            if (isIdle) {
                TextButton(
                    onClick = {
                        onEvent(DeleteAllProgressUiEvent.OnCancel)
                    },
                ) {
                    Text(text = stringResource(Res.string.cancel))
                }
            }
        },
    )
}

@Composable
private fun DeleteAllProgressConsequences() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(consequenceSpacing),
    ) {
        Text(
            text = stringResource(Res.string.delete_all_progress_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.REMOVED,
            text = stringResource(Res.string.delete_all_progress_removes_chapters),
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.REMOVED,
            text = stringResource(Res.string.delete_all_progress_removes_plans),
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.KEPT,
            text = stringResource(Res.string.delete_all_progress_keeps_account),
        )
    }
}

@Composable
private fun DeleteAllProgressStatus(uiState: DeleteAllProgressUiState) {
    val isDeleted = uiState == DeleteAllProgressUiState.Success
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(progressSpacing),
    ) {
        if (isDeleted) {
            Icon(
                modifier = Modifier.size(progressIconSize),
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.size(progressIconSize))
        }
        Text(
            text = stringResource(
                if (isDeleted) {
                    Res.string.delete_all_progress_deleted
                } else {
                    Res.string.delete_all_progress_deleting
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
