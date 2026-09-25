package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AppAlertDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String?,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    icon: ImageVector? = null,
) {
    AppAlertDialog(
        title = title,
        text = text,
        actions = listOfNotNull(
            AppAlertDialogAction(
                text = confirmText,
                style = if (isDestructive) {
                    AppAlertDialogActionStyle.DESTRUCTIVE
                } else {
                    AppAlertDialogActionStyle.DEFAULT
                },
                isEnabled = true,
                onClick = onConfirm,
            ),
            dismissText?.let { label ->
                AppAlertDialogAction(
                    text = label,
                    style = AppAlertDialogActionStyle.CANCEL,
                    isEnabled = true,
                    onClick = onDismiss,
                )
            },
        ),
        onDismissRequest = onDismiss,
        icon = icon?.let { imageVector ->
            {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
fun AppAlertDialog(
    title: String,
    text: String?,
    actions: List<AppAlertDialogAction>,
    onDismissRequest: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    textField: AppAlertDialogTextField? = null,
) {
    val resolution = remember { AlertDialogResolution() }
    PlatformAlertDialog(
        title = title,
        text = text,
        actions = actions.map { action ->
            action.copy(onClick = { resolution.resolve(action.onClick) })
        },
        onDismissRequest = { resolution.resolve(onDismissRequest) },
        icon = icon,
        textField = textField,
    )
    DisposableEffect(resolution) {
        onDispose(resolution::close)
    }
}

private class AlertDialogResolution {
    private var isOpen = true

    fun resolve(action: () -> Unit) {
        if (isOpen) {
            isOpen = false
            action()
        }
    }

    fun close() {
        isOpen = false
    }
}
