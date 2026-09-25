package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.runtime.Composable

@Composable
internal expect fun PlatformAlertDialog(
    title: String,
    text: String?,
    actions: List<AppAlertDialogAction>,
    onDismissRequest: () -> Unit,
    icon: (@Composable () -> Unit)?,
    textField: AppAlertDialogTextField?,
)
