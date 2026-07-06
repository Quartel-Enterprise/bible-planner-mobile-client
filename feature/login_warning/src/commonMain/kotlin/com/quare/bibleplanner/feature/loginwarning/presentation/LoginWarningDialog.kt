package com.quare.bibleplanner.feature.loginwarning.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import bibleplanner.feature.login_warning.generated.resources.Res
import bibleplanner.feature.login_warning.generated.resources.login_warning_dismiss
import bibleplanner.feature.login_warning.generated.resources.login_warning_login
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_day_study
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_language
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_purchase
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_theme
import bibleplanner.feature.login_warning.generated.resources.login_warning_title
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.feature.loginwarning.presentation.model.LoginWarningUiEvent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginWarningDialog(
    reason: LoginWarningReason,
    onEvent: (LoginWarningUiEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(LoginWarningUiEvent.OnDismiss) },
        title = {
            Text(
                text = stringResource(Res.string.login_warning_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = stringResource(reason.toMessageResource()),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = { onEvent(LoginWarningUiEvent.OnLoginClick) }) {
                Text(text = stringResource(Res.string.login_warning_login))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(LoginWarningUiEvent.OnDismiss) }) {
                Text(text = stringResource(Res.string.login_warning_dismiss))
            }
        },
    )
}

private fun LoginWarningReason.toMessageResource(): StringResource = when (this) {
    LoginWarningReason.Purchase -> Res.string.login_warning_message_purchase
    LoginWarningReason.DayStudy -> Res.string.login_warning_message_day_study
    LoginWarningReason.Preferences.Theme -> Res.string.login_warning_message_theme
    LoginWarningReason.Preferences.Language -> Res.string.login_warning_message_language
}
