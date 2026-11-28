package com.quare.bibleplanner.ui.component.date

import androidx.compose.runtime.Composable

@Composable
actual fun PickerDialogSceneContent(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    content()
}
