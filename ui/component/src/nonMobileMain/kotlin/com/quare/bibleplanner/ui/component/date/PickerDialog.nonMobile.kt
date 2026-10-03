package com.quare.bibleplanner.ui.component.date

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
actual fun PickerDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = pickerDialogProperties,
        content = content,
    )
}
