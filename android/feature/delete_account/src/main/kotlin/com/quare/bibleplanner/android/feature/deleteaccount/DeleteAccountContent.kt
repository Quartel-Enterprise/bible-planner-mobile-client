package com.quare.bibleplanner.android.feature.deleteaccount

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.quare.bibleplanner.android.designsystem.dialog.DialogConsequenceRow
import com.quare.bibleplanner.android.designsystem.dialog.DialogConsequenceType
import com.quare.bibleplanner.android.designsystem.dialog.HideSceneDialogScrimEffect
import com.quare.bibleplanner.android.designsystem.dialog.rememberAlertDialogResolution
import com.quare.bibleplanner.android.designsystem.theme.success
import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore
import com.quare.bibleplanner.feature.deleteaccount.domain.usecase.DeleteAccountPhase
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiEvent
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiState
import com.quare.bibleplanner.feature.deleteaccount.presentation.model.DeleteAccountUiStatus

private val contentSpacing = 12.dp
private val warningSpacing = 10.dp
private val warningPadding = 12.dp
private val warningIconSize = 18.dp
private val progressIconSize = 44.dp
private val progressSpacing = 18.dp

@Composable
internal fun DeleteAccountContent(
    uiState: DeleteAccountUiState,
    onEvent: (DeleteAccountUiEvent) -> Unit,
) {
    val isIdle = uiState.status == DeleteAccountUiStatus.Idle
    val resolution = rememberAlertDialogResolution(isIdle)
    HideSceneDialogScrimEffect()
    AlertDialog(
        onDismissRequest = { resolution.resolve { onEvent(DeleteAccountUiEvent.OnDismiss) } },
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
                    text = stringResource(R.string.delete_account_title),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            null
        },
        text = {
            when (val status = uiState.status) {
                DeleteAccountUiStatus.Idle -> ConfirmationColumn(
                    uiState = uiState,
                    onEvent = onEvent,
                )

                is DeleteAccountUiStatus.Deleting -> StatusColumn(phase = status.phase)

                DeleteAccountUiStatus.Deleted -> StatusColumn(phase = null)
            }
        },
        confirmButton = {
            if (isIdle) {
                /*
                 * Why: a deletion that fails within one frame goes back to Idle before the dialog ever draws
                 * Deleting, so a resolution closed by this tap would never reopen and leave the dialog stuck.
                 * The ViewModel ignores a second confirm while it deletes.
                 */
                TextButton(
                    onClick = { onEvent(DeleteAccountUiEvent.OnConfirmDelete) },
                    enabled = uiState.isConfirmEnabled,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(text = stringResource(R.string.delete_account_confirm))
                }
            }
        },
        dismissButton = {
            if (isIdle) {
                TextButton(onClick = { resolution.resolve { onEvent(DeleteAccountUiEvent.OnCancel) } }) {
                    Text(text = stringResource(R.string.delete_account_cancel))
                }
            }
        },
    )
}

@Composable
private fun ConfirmationColumn(
    uiState: DeleteAccountUiState,
    onEvent: (DeleteAccountUiEvent) -> Unit,
) {
    val keyword = stringResource(R.string.delete_account_confirmation_keyword)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        Text(
            text = stringResource(R.string.delete_account_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.REMOVED,
            text = stringResource(R.string.delete_account_removes_progress),
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.REMOVED,
            text = stringResource(R.string.delete_account_removes_notes),
        )
        DialogConsequenceRow(
            type = DialogConsequenceType.REMOVED,
            text = stringResource(R.string.delete_account_removes_preferences),
        )
        uiState.subscriptionStore?.let { store ->
            SubscriptionWarningSurface(store = store)
        }
        Text(
            text = stringResource(R.string.delete_account_servers_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.delete_account_confirmation_label, keyword),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.confirmationText,
            onValueChange = { typed -> onEvent(DeleteAccountUiEvent.OnConfirmationTextChange(typed)) },
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
private fun SubscriptionWarningSurface(store: PurchaseStore) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
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
                    R.string.delete_account_subscription_warning,
                    stringResource(store.toStoreNameRes()),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusColumn(phase: DeleteAccountPhase?) {
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
                tint = MaterialTheme.colorScheme.success,
            )
        } else {
            LoadingIndicator(modifier = Modifier.size(progressIconSize))
        }
        Text(
            text = stringResource(phase.toMessageRes()),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@StringRes
private fun DeleteAccountPhase?.toMessageRes(): Int = when (this) {
    DeleteAccountPhase.DELETING_DATA -> R.string.delete_account_deleting_data
    DeleteAccountPhase.CLOSING_ACCOUNT -> R.string.delete_account_closing
    null -> R.string.delete_account_deleted
}
