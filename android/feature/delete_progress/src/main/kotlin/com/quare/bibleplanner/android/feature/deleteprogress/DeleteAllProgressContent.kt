package com.quare.bibleplanner.android.feature.deleteprogress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.quare.bibleplanner.android.designsystem.dialog.DialogConsequenceRow
import com.quare.bibleplanner.android.designsystem.dialog.DialogConsequenceType
import com.quare.bibleplanner.android.designsystem.dialog.rememberAlertDialogResolution
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiEvent
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiState

private val consequenceSpacing = 10.dp
private val progressIconSize = 44.dp
private val progressSpacing = 18.dp

@Composable
internal fun DeleteAllProgressContent(
    uiState: DeleteAllProgressUiState,
    onEvent: (DeleteAllProgressUiEvent) -> Unit,
) {
    val isIdle = uiState == DeleteAllProgressUiState.Idle
    // Why: a failed reset returns to Idle, where the user may confirm again.
    val resolution = rememberAlertDialogResolution(isIdle)
    AlertDialog(
        onDismissRequest = { resolution.resolve { onEvent(DeleteAllProgressUiEvent.OnCancel) } },
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
                    text = stringResource(R.string.delete_all_progress_title),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            null
        },
        text = {
            if (isIdle) {
                ConsequencesColumn()
            } else {
                StatusColumn(isDeleted = uiState == DeleteAllProgressUiState.Success)
            }
        },
        confirmButton = {
            if (isIdle) {
                TextButton(onClick = { resolution.resolve { onEvent(DeleteAllProgressUiEvent.OnConfirmDelete) } }) {
                    Text(
                        text = stringResource(R.string.delete_all_progress_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        dismissButton = {
            if (isIdle) {
                TextButton(onClick = { resolution.resolve { onEvent(DeleteAllProgressUiEvent.OnCancel) } }) {
                    Text(text = stringResource(R.string.delete_all_progress_cancel))
                }
            }
        },
    )
}

@Composable
private fun ConsequencesColumn() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(consequenceSpacing),
    ) {
        Text(
            text = stringResource(R.string.delete_all_progress_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.REMOVED,
            text = stringResource(R.string.delete_all_progress_removes_chapters),
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.REMOVED,
            text = stringResource(R.string.delete_all_progress_removes_plans),
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.KEPT,
            text = stringResource(R.string.delete_all_progress_keeps_account),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusColumn(isDeleted: Boolean) {
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
            LoadingIndicator(modifier = Modifier.size(progressIconSize))
        }
        Text(
            text = stringResource(
                if (isDeleted) R.string.delete_all_progress_deleted else R.string.delete_all_progress_deleting,
            ),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
