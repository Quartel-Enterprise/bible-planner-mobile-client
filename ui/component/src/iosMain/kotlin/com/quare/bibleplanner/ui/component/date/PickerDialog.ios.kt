package com.quare.bibleplanner.ui.component.date

import androidx.compose.runtime.Composable

@Composable
actual fun PickerDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    NativePickerModal(
        onDismissRequest = onDismissRequest,
        content = content,
    )
}
