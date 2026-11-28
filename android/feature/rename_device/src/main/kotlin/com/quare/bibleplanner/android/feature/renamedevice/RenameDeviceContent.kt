package com.quare.bibleplanner.android.feature.renamedevice

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.feature.accountdetails.presentation.model.RenameDeviceUiEvent

@Composable
internal fun RenameDeviceContent(
    initialName: String,
    onEvent: (RenameDeviceUiEvent) -> Unit,
) {
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = initialName,
                selection = TextRange(0, initialName.length),
            ),
        )
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
    AppAlertDialog(
        title = stringResource(R.string.rename_device_title),
        confirmText = stringResource(R.string.rename_device_save),
        onConfirm = { onEvent(RenameDeviceUiEvent.OnConfirmClick(name.text)) },
        dismissText = stringResource(R.string.rename_device_cancel),
        onDismiss = { onEvent(RenameDeviceUiEvent.OnDismiss) },
        isConfirmEnabled = name.text.isNotBlank(),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = name,
            onValueChange = { typed -> name = typed },
            label = { Text(text = stringResource(R.string.rename_device_name_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
        )
    }
}
