package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.runtime.Composable

@Composable
internal actual fun PlatformAlertDialog(
    title: String,
    text: String?,
    actions: List<AppAlertDialogAction>,
    onDismissRequest: () -> Unit,
    icon: (@Composable () -> Unit)?,
    textField: AppAlertDialogTextField?,
) {
    MaterialAlertDialog(
        title = title,
        text = text,
        actions = actions,
        onDismissRequest = onDismissRequest,
        icon = icon,
        textField = textField,
    )
}
