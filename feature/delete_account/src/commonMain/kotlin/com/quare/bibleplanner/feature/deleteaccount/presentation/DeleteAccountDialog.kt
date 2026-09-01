package com.quare.bibleplanner.feature.deleteaccount.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import bibleplanner.feature.delete_account.generated.resources.Res
import bibleplanner.feature.delete_account.generated.resources.delete_account_cancel
import bibleplanner.feature.delete_account.generated.resources.delete_account_closing
import bibleplanner.feature.delete_account.generated.resources.delete_account_confirm
import bibleplanner.feature.delete_account.generated.resources.delete_account_confirmation_keyword
import bibleplanner.feature.delete_account.generated.resources.delete_account_confirmation_label
import bibleplanner.feature.delete_account.generated.resources.delete_account_deleted
import bibleplanner.feature.delete_account.generated.resources.delete_account_deleting_data
import bibleplanner.feature.delete_account.generated.resources.delete_account_message
import bibleplanner.feature.delete_account.generated.resources.delete_account_removes_notes
import bibleplanner.feature.delete_account.generated.resources.delete_account_removes_preferences
import bibleplanner.feature.delete_account.generated.resources.delete_account_removes_progress
import bibleplanner.feature.delete_account.generated.resources.delete_account_servers_note
import bibleplanner.feature.delete_account.generated.resources.delete_account_subscription_warning
import bibleplanner.feature.delete_account.generated.resources.delete_account_title
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.DeleteAccountPhase
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiEvent
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiState
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiStatus
import com.quare.bibleplanner.ui.component.dialog.DialogConsequenceRow
import com.quare.bibleplanner.ui.component.dialog.DialogConsequenceType
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val contentSpacing = 12.dp
private val warningSpacing = 10.dp
private val warningPadding = 12.dp
private val warningCornerRadius = 12.dp
private val warningIconSize = 18.dp
private val progressIconSize = 44.dp
private val progressSpacing = 18.dp

@Composable
internal fun DeleteAccountDialog(
    uiState: DeleteAccountUiState,
    onEvent: (DeleteAccountUiEvent) -> Unit,
) {
    val isIdle = uiState.status == DeleteAccountUiStatus.Idle
    AlertDialog(
        onDismissRequest = {
            onEvent(DeleteAccountUiEvent.OnDismiss)
        },
        properties = DialogProperties(
            dismissOnBackPress = isIdle,
            dismissOnClickOutside = isIdle,
        ),
        icon = if (isIdle) {
            {
                Icon(
                    imageVector = Icons.Default.PersonRemove,
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
                    text = stringResource(Res.string.delete_account_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            null
        },
        text = {
            when (val status = uiState.status) {
                DeleteAccountUiStatus.Idle -> DeleteAccountConfirmationContent(
                    uiState = uiState,
                    onEvent = onEvent,
                )

                is DeleteAccountUiStatus.Deleting -> DeleteAccountStatusContent(phase = status.phase)

                DeleteAccountUiStatus.Deleted -> DeleteAccountStatusContent(phase = null)
            }
        },
        confirmButton = {
            if (isIdle) {
                TextButton(
                    enabled = uiState.isConfirmEnabled,
                    onClick = {
                        onEvent(DeleteAccountUiEvent.OnConfirmDelete)
                    },
                ) {
                    Text(
                        text = stringResource(Res.string.delete_account_confirm),
                        color = if (uiState.isConfirmEnabled) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color.Unspecified
                        },
                    )
                }
            }
        },
        dismissButton = {
            if (isIdle) {
                TextButton(
                    onClick = {
                        onEvent(DeleteAccountUiEvent.OnCancel)
                    },
                ) {
                    Text(text = stringResource(Res.string.delete_account_cancel))
                }
            }
        },
    )
}

@Composable
private fun DeleteAccountConfirmationContent(
    uiState: DeleteAccountUiState,
    onEvent: (DeleteAccountUiEvent) -> Unit,
) {
    val keyword = stringResource(Res.string.delete_account_confirmation_keyword)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        Text(
            text = stringResource(Res.string.delete_account_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.REMOVED,
            text = stringResource(Res.string.delete_account_removes_progress),
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.REMOVED,
            text = stringResource(Res.string.delete_account_removes_notes),
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.REMOVED,
            text = stringResource(Res.string.delete_account_removes_preferences),
        )
        uiState.storeNameRes?.let { storeNameRes ->
            SubscriptionWarning(storeNameRes = storeNameRes)
        }
        Text(
            text = stringResource(Res.string.delete_account_servers_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.delete_account_confirmation_label, keyword),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.confirmationText,
            onValueChange = { onEvent(DeleteAccountUiEvent.OnConfirmationTextChange(it)) },
            singleLine = true,
            placeholder = { Text(text = keyword) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
        )
    }
}

@Composable
private fun SubscriptionWarning(storeNameRes: StringResource) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(warningCornerRadius),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(warningPadding),
            horizontalArrangement = Arrangement.spacedBy(warningSpacing),
        ) {
            Icon(
                modifier = Modifier.size(warningIconSize),
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    Res.string.delete_account_subscription_warning,
                    stringResource(storeNameRes),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DeleteAccountStatusContent(phase: DeleteAccountPhase?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(progressSpacing),
    ) {
        if (phase == null) {
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
            text = stringResource(phase.toMessageRes()),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

private fun DeleteAccountPhase?.toMessageRes(): StringResource = when (this) {
    DeleteAccountPhase.DELETING_DATA -> Res.string.delete_account_deleting_data
    DeleteAccountPhase.CLOSING_ACCOUNT -> Res.string.delete_account_closing
    null -> Res.string.delete_account_deleted
}
