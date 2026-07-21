package com.quare.bibleplanner.feature.editprofile.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_cancel
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_name_counter
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_name_placeholder
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_name_subtitle
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_name_title
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_save
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditNameUiEvent
import org.jetbrains.compose.resources.stringResource

internal const val MAX_DISPLAY_NAME_LENGTH = 50

@Composable
internal fun EditNameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onEvent: (EditNameUiEvent) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.edit_profile_name_title)) },
        text = {
            Column {
                Text(stringResource(Res.string.edit_profile_name_subtitle))
                OutlinedTextField(
                    value = name,
                    onValueChange = { typed -> name = typed.take(MAX_DISPLAY_NAME_LENGTH) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(Res.string.edit_profile_name_placeholder)) },
                    supportingText = {
                        Text(
                            text = stringResource(
                                Res.string.edit_profile_name_counter,
                                name.length,
                                MAX_DISPLAY_NAME_LENGTH,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(EditNameUiEvent.OnSaveClick(name)) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(Res.string.edit_profile_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.edit_profile_cancel))
            }
        },
    )
}
