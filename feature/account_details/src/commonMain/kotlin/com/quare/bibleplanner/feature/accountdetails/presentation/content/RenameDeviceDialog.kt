package com.quare.bibleplanner.feature.accountdetails.presentation.content

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
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialogAction
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialogActionStyle
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialogTextField
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RenameDeviceDialog(
    initialName: String,
    onEvent: (RenameDeviceUiEvent) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    AppAlertDialog(
        title = stringResource(Res.string.account_details_rename_title),
        text = null,
        actions = listOf(
            AppAlertDialogAction(
                text = stringResource(Res.string.account_details_rename_confirm),
                style = AppAlertDialogActionStyle.DEFAULT,
                isEnabled = name.isNotBlank(),
                onClick = { onEvent(RenameDeviceUiEvent.OnConfirmClick(name)) },
            ),
            AppAlertDialogAction(
                text = stringResource(Res.string.account_details_rename_cancel),
                style = AppAlertDialogActionStyle.CANCEL,
                isEnabled = true,
                onClick = { onEvent(RenameDeviceUiEvent.OnDismiss) },
            ),
        ),
        onDismissRequest = { onEvent(RenameDeviceUiEvent.OnDismiss) },
        textField = AppAlertDialogTextField(
            value = name,
            label = stringResource(Res.string.account_details_rename_placeholder),
            onValueChange = { typed -> name = typed },
        ),
    )
}
