package com.quare.bibleplanner.android.feature.loginwarning

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.feature.loginwarning.presentation.model.LoginWarningUiEvent

@Composable
internal fun LoginWarningContent(
    reason: LoginWarningReason,
    onEvent: (LoginWarningUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = stringResource(R.string.login_warning_title),
        text = stringResource(reason.toMessageResource()),
        confirmText = stringResource(R.string.login_warning_login),
        onConfirm = { onEvent(LoginWarningUiEvent.OnLoginClick) },
        dismissText = stringResource(R.string.login_warning_dismiss),
        onDismiss = { onEvent(LoginWarningUiEvent.OnDismiss) },
    )
}

@StringRes
private fun LoginWarningReason.toMessageResource(): Int = when (this) {
    LoginWarningReason.Purchase -> R.string.login_warning_message_purchase
    LoginWarningReason.DayStudy, LoginWarningReason.ChapterStudy -> R.string.login_warning_message_day_study
    LoginWarningReason.AiChat -> R.string.login_warning_message_ai_chat
    LoginWarningReason.ChapterListening -> R.string.login_warning_message_chapter_listening
    LoginWarningReason.Preferences.Theme -> R.string.login_warning_message_theme
    LoginWarningReason.Preferences.Language -> R.string.login_warning_message_language
    LoginWarningReason.Preferences.StudySuggestion -> R.string.login_warning_message_study_suggestion
}
