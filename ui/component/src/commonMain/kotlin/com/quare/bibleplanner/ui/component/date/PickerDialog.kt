package com.quare.bibleplanner.ui.component.date

import androidx.compose.runtime.Composable

@Composable
expect fun PickerDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
)
