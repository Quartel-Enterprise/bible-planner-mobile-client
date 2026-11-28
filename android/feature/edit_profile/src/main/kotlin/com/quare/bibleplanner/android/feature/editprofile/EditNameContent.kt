package com.quare.bibleplanner.android.feature.editprofile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditNameUiEvent

private const val MAX_DISPLAY_NAME_LENGTH = 50

@Composable
internal fun EditNameContent(
    initialName: String,
    onDismiss: () -> Unit,
    onEvent: (EditNameUiEvent) -> Unit,
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
    AppAlertDialog(
        title = stringResource(R.string.edit_profile_name_title),
        confirmText = stringResource(R.string.edit_profile_save),
        onConfirm = { onEvent(EditNameUiEvent.OnSaveClick(name.text)) },
        dismissText = stringResource(R.string.edit_profile_cancel),
        onDismiss = onDismiss,
        isConfirmEnabled = name.text.isNotBlank(),
    ) {
        // Why: the alert composes later in its own window, so a request from outside could beat the field.
        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = stringResource(R.string.edit_profile_name_subtitle))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = name,
                onValueChange = { typed -> name = typed.toTruncated(MAX_DISPLAY_NAME_LENGTH) },
                label = { Text(text = stringResource(R.string.edit_profile_name_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
            )
        }
    }
}

private fun TextFieldValue.toTruncated(maxLength: Int): TextFieldValue = if (text.length <= maxLength) {
    this
} else {
    copy(
        text = text.take(maxLength),
        selection = TextRange(
            start = selection.start.coerceAtMost(maxLength),
            end = selection.end.coerceAtMost(maxLength),
        ),
        composition = null,
    )
}
