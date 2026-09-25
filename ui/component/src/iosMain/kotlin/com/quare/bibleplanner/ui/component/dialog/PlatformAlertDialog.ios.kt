package com.quare.bibleplanner.ui.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dialog.AdaptiveBasicAlertDialog
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosAction
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosProperties
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosTextField

@OptIn(ExperimentalCalfUiApi::class)
@Composable
internal actual fun PlatformAlertDialog(
    title: String,
    text: String?,
    actions: List<AppAlertDialogAction>,
    onDismissRequest: () -> Unit,
    icon: (@Composable () -> Unit)?,
    textField: AppAlertDialogTextField?,
) {
    val latestActions by rememberUpdatedState(actions)
    val latestTextField by rememberUpdatedState(textField)
    AdaptiveBasicAlertDialog(
        onDismissRequest = onDismissRequest,
        iosProperties = AlertDialogIosProperties(
            title = title,
            text = text.orEmpty(),
            style = AlertDialogIosStyle.Alert,
            actions = actions.mapIndexed { index, action ->
                AlertDialogIosAction(
                    title = action.text,
                    style = action.style.toIosActionStyle(),
                    onClick = { latestActions[index].onClick() },
                    enabled = action.isEnabled,
                )
            },
            textFields = listOfNotNull(
                textField?.let { field ->
                    AlertDialogIosTextField(
                        placeholder = field.label,
                        initialValue = field.value,
                        onValueChange = { value -> latestTextField?.onValueChange(value) },
                    )
                },
            ),
        ),
        materialContent = {},
    )
}

private fun AppAlertDialogActionStyle.toIosActionStyle(): AlertDialogIosActionStyle = when (this) {
    AppAlertDialogActionStyle.DEFAULT -> AlertDialogIosActionStyle.Default
    AppAlertDialogActionStyle.DESTRUCTIVE -> AlertDialogIosActionStyle.Destructive
    AppAlertDialogActionStyle.CANCEL -> AlertDialogIosActionStyle.Cancel
}
