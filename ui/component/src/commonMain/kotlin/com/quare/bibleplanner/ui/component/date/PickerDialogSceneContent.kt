package com.quare.bibleplanner.ui.component.date

import androidx.compose.runtime.Composable

@Composable
expect fun PickerDialogSceneContent(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
)
