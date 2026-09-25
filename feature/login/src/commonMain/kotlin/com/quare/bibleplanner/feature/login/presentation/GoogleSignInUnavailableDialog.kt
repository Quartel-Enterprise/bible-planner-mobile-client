package com.quare.bibleplanner.feature.login.presentation

import androidx.compose.runtime.Composable
import bibleplanner.feature.login.generated.resources.Res
import bibleplanner.feature.login.generated.resources.google_sign_in_unavailable_confirm
import bibleplanner.feature.login.generated.resources.google_sign_in_unavailable_dismiss
import bibleplanner.feature.login.generated.resources.google_sign_in_unavailable_message
import bibleplanner.feature.login.generated.resources.google_sign_in_unavailable_title
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoogleSignInUnavailableDialog(onEvent: (LoginUiEvent) -> Unit) {
    AppAlertDialog(
        title = stringResource(Res.string.google_sign_in_unavailable_title),
        text = stringResource(Res.string.google_sign_in_unavailable_message),
        confirmText = stringResource(Res.string.google_sign_in_unavailable_confirm),
        onConfirm = { onEvent(LoginUiEvent.AddGoogleAccountConfirmClick) },
        dismissText = stringResource(Res.string.google_sign_in_unavailable_dismiss),
        onDismiss = { onEvent(LoginUiEvent.DismissAddGoogleAccountDialog) },
    )
}
