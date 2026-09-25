package com.quare.bibleplanner.feature.editprofile.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_cancel
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_name_placeholder
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_name_subtitle
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_name_title
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_save
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditNameUiEvent
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialogAction
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialogActionStyle
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialogTextField
import org.jetbrains.compose.resources.stringResource

private const val MAX_DISPLAY_NAME_LENGTH = 50

@Composable
internal fun EditNameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onEvent: (EditNameUiEvent) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    AppAlertDialog(
        title = stringResource(Res.string.edit_profile_name_title),
        text = stringResource(Res.string.edit_profile_name_subtitle),
        actions = listOf(
            AppAlertDialogAction(
                text = stringResource(Res.string.edit_profile_save),
                style = AppAlertDialogActionStyle.DEFAULT,
                isEnabled = name.isNotBlank(),
                onClick = { onEvent(EditNameUiEvent.OnSaveClick(name)) },
            ),
            AppAlertDialogAction(
                text = stringResource(Res.string.edit_profile_cancel),
                style = AppAlertDialogActionStyle.CANCEL,
                isEnabled = true,
                onClick = onDismiss,
            ),
        ),
        onDismissRequest = onDismiss,
        textField = AppAlertDialogTextField(
            value = name,
            label = stringResource(Res.string.edit_profile_name_placeholder),
            onValueChange = { typed -> name = typed.take(MAX_DISPLAY_NAME_LENGTH) },
        ),
    )
}
