package com.quare.bibleplanner.android.designsystem.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

@Composable
fun AppAlertDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String?,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
) {
    AppAlertDialog(
        title = title,
        confirmText = confirmText,
        onConfirm = onConfirm,
        dismissText = dismissText,
        onDismiss = onDismiss,
        isDestructive = isDestructive,
        text = { Text(text = text) },
    )
}

@Composable
fun AppAlertDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String?,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    onDismissRequest: () -> Unit = onDismiss,
    text: @Composable () -> Unit,
) {
    val resolution = remember { AlertDialogResolution() }
    AlertDialog(
        onDismissRequest = { resolution.resolve(onDismissRequest) },
        title = { Text(text = title) },
        text = text,
        confirmButton = {
            TextButton(
                onClick = { resolution.resolve(onConfirm) },
                colors = if (isDestructive) {
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.textButtonColors()
                },
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = dismissText?.let { label ->
            {
                TextButton(onClick = { resolution.resolve(onDismiss) }) {
                    Text(text = label)
                }
            }
        },
    )
    DisposableEffect(resolution) {
        onDispose(resolution::close)
    }
}

// Why: a double tap, or a tap while the dialog leaves, must not confirm or dismiss twice.
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
