package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle

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
    val resolution = remember { AlertDialogResolution() }
    val resolveConfirm = { resolution.resolve(onConfirm) }
    val resolveDismiss = { resolution.resolve(onDismiss) }
    AdaptiveAlertDialog(
        onConfirm = resolveConfirm,
        onDismiss = resolveDismiss,
        confirmText = confirmText,
        dismissText = dismissText,
        title = title,
        text = text,
        materialConfirmButton = {
            TextButton(onClick = resolveConfirm) {
                Text(confirmText)
            }
        },
        materialDismissButton = dismissText?.let { label ->
            {
                TextButton(onClick = resolveDismiss) {
                    Text(label)
                }
            }
        },
        materialIcon = icon?.let { imageVector ->
            {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                )
            }
        },
        iosConfirmButtonStyle = if (isDestructive) {
            AlertDialogIosActionStyle.Destructive
        } else {
            AlertDialogIosActionStyle.Default
        },
        iosDismissButtonStyle = AlertDialogIosActionStyle.Cancel,
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
