package com.quare.bibleplanner.feature.login.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import bibleplanner.feature.login.generated.resources.Res
import bibleplanner.feature.login.generated.resources.google_sign_in_unavailable_confirm
import bibleplanner.feature.login.generated.resources.google_sign_in_unavailable_dismiss
import bibleplanner.feature.login.generated.resources.google_sign_in_unavailable_message
import bibleplanner.feature.login.generated.resources.google_sign_in_unavailable_title
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoogleSignInUnavailableDialog(onEvent: (LoginUiEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(LoginUiEvent.DismissAddGoogleAccountDialog) },
        title = {
            Text(
                text = stringResource(Res.string.google_sign_in_unavailable_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.google_sign_in_unavailable_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = { onEvent(LoginUiEvent.AddGoogleAccountConfirmClick) }) {
                Text(text = stringResource(Res.string.google_sign_in_unavailable_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(LoginUiEvent.DismissAddGoogleAccountDialog) }) {
                Text(text = stringResource(Res.string.google_sign_in_unavailable_dismiss))
            }
        },
    )
}
