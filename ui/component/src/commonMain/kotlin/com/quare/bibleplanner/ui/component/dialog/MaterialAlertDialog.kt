package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val MAX_INLINE_ACTIONS = 2
private val contentSpacing = 16.dp

@Composable
internal fun MaterialAlertDialog(
    title: String,
    text: String?,
    actions: List<AppAlertDialogAction>,
    onDismissRequest: () -> Unit,
    icon: (@Composable () -> Unit)?,
    textField: AppAlertDialogTextField?,
) {
    val isStacked = actions.size > MAX_INLINE_ACTIONS
    val cancelAction = actions.firstOrNull { action -> action.style == AppAlertDialogActionStyle.CANCEL }
    val confirmActions = actions.filterNot { action -> action === cancelAction }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = icon,
        title = { Text(text = title) },
        text = if (text == null && textField == null) {
            null
        } else {
            {
                MaterialAlertDialogContent(
                    text = text,
                    textField = textField,
                )
            }
        },
        confirmButton = {
            if (isStacked) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                ) {
                    actions.forEach { action ->
                        MaterialAlertDialogActionButton(action = action)
                    }
                }
            } else {
                confirmActions.forEach { action ->
                    MaterialAlertDialogActionButton(action = action)
                }
            }
        },
        dismissButton = cancelAction?.takeUnless { isStacked }?.let { action ->
            { MaterialAlertDialogActionButton(action = action) }
        },
    )
}

@Composable
private fun MaterialAlertDialogContent(
    text: String?,
    textField: AppAlertDialogTextField?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(contentSpacing)) {
        text?.let { message ->
            Text(text = message)
        }
        textField?.let { field ->
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = field.value,
                onValueChange = field.onValueChange,
                singleLine = true,
                label = { Text(text = field.label) },
            )
        }
    }
}

@Composable
private fun MaterialAlertDialogActionButton(action: AppAlertDialogAction) {
    TextButton(
        onClick = action.onClick,
        enabled = action.isEnabled,
    ) {
        Text(
            text = action.text,
            color = if (action.style == AppAlertDialogActionStyle.DESTRUCTIVE && action.isEnabled) {
                MaterialTheme.colorScheme.error
            } else {
                Color.Unspecified
            },
        )
    }
}
