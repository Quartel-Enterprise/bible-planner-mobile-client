package com.quare.bibleplanner.android.feature.logout

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.android.designsystem.dialog.AppProgressDialog
import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutPhase
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiEvent
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiState

@Composable
internal fun LogoutContent(
    uiState: LogoutUiState,
    onEvent: (LogoutUiEvent) -> Unit,
) {
    when (uiState) {
        LogoutUiState.Idle -> LogoutConfirmationDialog(
            title = stringResource(R.string.logout_title),
            text = stringResource(R.string.logout_message),
            confirmText = stringResource(R.string.logout_confirm),
            confirmEvent = LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout,
            isDestructive = false,
            onEvent = onEvent,
        )

        LogoutUiState.PendingChangesError -> LogoutConfirmationDialog(
            title = stringResource(R.string.logout_pending_changes_error_title),
            text = stringResource(
                R.string.logout_pending_changes_error_message,
                stringResource(R.string.logout_pending_favorites),
            ),
            confirmText = stringResource(R.string.logout_sign_out_anyway),
            confirmEvent = LogoutUiEvent.ConfirmLogoutClick.OnForceLogout,
            isDestructive = true,
            onEvent = onEvent,
        )

        is LogoutUiState.Loading -> AppProgressDialog(
            message = stringResource(
                when (uiState.phase) {
                    LogoutPhase.SYNCING -> R.string.logout_syncing_progress
                    LogoutPhase.ENDING_SESSION -> R.string.logout_ending_session
                },
            ),
        )
    }
}

@Composable
private fun LogoutConfirmationDialog(
    title: String,
    text: String,
    confirmText: String,
    confirmEvent: LogoutUiEvent.ConfirmLogoutClick,
    isDestructive: Boolean,
    onEvent: (LogoutUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = title,
        confirmText = confirmText,
        onConfirm = { onEvent(confirmEvent) },
        dismissText = stringResource(R.string.logout_cancel),
        onDismiss = { onEvent(LogoutUiEvent.OnCancel) },
        isDestructive = isDestructive,
        onDismissRequest = { onEvent(LogoutUiEvent.OnDismiss) },
    ) {
        Text(text = text)
    }
}
