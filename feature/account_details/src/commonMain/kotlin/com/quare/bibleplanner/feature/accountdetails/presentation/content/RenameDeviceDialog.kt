package com.quare.bibleplanner.feature.accountdetails.presentation.content

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import bibleplanner.feature.account_details.generated.resources.Res
import bibleplanner.feature.account_details.generated.resources.account_details_rename_cancel
import bibleplanner.feature.account_details.generated.resources.account_details_rename_confirm
import bibleplanner.feature.account_details.generated.resources.account_details_rename_placeholder
import bibleplanner.feature.account_details.generated.resources.account_details_rename_title
import com.quare.bibleplanner.feature.accountdetails.presentation.model.RenameDeviceUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RenameDeviceDialog(
    initialName: String,
    onEvent: (RenameDeviceUiEvent) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = { onEvent(RenameDeviceUiEvent.OnDismiss) },
        title = { Text(stringResource(Res.string.account_details_rename_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(Res.string.account_details_rename_placeholder)) },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(RenameDeviceUiEvent.OnConfirmClick(name)) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(Res.string.account_details_rename_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(RenameDeviceUiEvent.OnDismiss) }) {
                Text(stringResource(Res.string.account_details_rename_cancel))
            }
        },
    )
}
