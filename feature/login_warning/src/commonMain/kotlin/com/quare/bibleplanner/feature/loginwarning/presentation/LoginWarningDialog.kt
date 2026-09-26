package com.quare.bibleplanner.feature.loginwarning.presentation

import androidx.compose.runtime.Composable
import bibleplanner.feature.login_warning.generated.resources.Res
import bibleplanner.feature.login_warning.generated.resources.login_warning_dismiss
import bibleplanner.feature.login_warning.generated.resources.login_warning_login
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_ai_chat
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_day_study
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_language
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_purchase
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_study_suggestion
import bibleplanner.feature.login_warning.generated.resources.login_warning_message_theme
import bibleplanner.feature.login_warning.generated.resources.login_warning_title
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.feature.loginwarning.presentation.model.LoginWarningUiEvent
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginWarningDialog(
    reason: LoginWarningReason,
    onEvent: (LoginWarningUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = stringResource(Res.string.login_warning_title),
        text = stringResource(reason.toMessageResource()),
        confirmText = stringResource(Res.string.login_warning_login),
        onConfirm = { onEvent(LoginWarningUiEvent.OnLoginClick) },
        dismissText = stringResource(Res.string.login_warning_dismiss),
        onDismiss = { onEvent(LoginWarningUiEvent.OnDismiss) },
    )
}

internal fun LoginWarningReason.toMessageResource(): StringResource = when (this) {
    LoginWarningReason.Purchase -> Res.string.login_warning_message_purchase
    LoginWarningReason.DayStudy -> Res.string.login_warning_message_day_study
    LoginWarningReason.AiChat -> Res.string.login_warning_message_ai_chat
    LoginWarningReason.Preferences.Theme -> Res.string.login_warning_message_theme
    LoginWarningReason.Preferences.Language -> Res.string.login_warning_message_language
    LoginWarningReason.Preferences.StudySuggestion -> Res.string.login_warning_message_study_suggestion
}
