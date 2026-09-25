package com.quare.bibleplanner.feature.logout.presentation

import androidx.compose.runtime.Composable
import bibleplanner.feature.logout.generated.resources.Res
import bibleplanner.feature.logout.generated.resources.logout_cancel
import bibleplanner.feature.logout.generated.resources.logout_confirm
import bibleplanner.feature.logout.generated.resources.logout_ending_session
import bibleplanner.feature.logout.generated.resources.logout_message
import bibleplanner.feature.logout.generated.resources.logout_pending_changes_error_message
import bibleplanner.feature.logout.generated.resources.logout_pending_changes_error_title
import bibleplanner.feature.logout.generated.resources.logout_sign_out_anyway
import bibleplanner.feature.logout.generated.resources.logout_syncing_progress
import bibleplanner.feature.logout.generated.resources.logout_title
import com.quare.bibleplanner.feature.logout.domain.usecase.LogoutPhase
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiEvent
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiState
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialogAction
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialogActionStyle
import com.quare.bibleplanner.ui.component.dialog.AppProgressDialog
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LogoutDialog(
    uiState: LogoutUiState,
    onEvent: (LogoutUiEvent) -> Unit,
) {
    when (uiState) {
        LogoutUiState.Idle -> LogoutConfirmationDialog(
            title = stringResource(Res.string.logout_title),
            text = stringResource(Res.string.logout_message),
            confirmAction = AppAlertDialogAction(
                text = stringResource(Res.string.logout_confirm),
                style = AppAlertDialogActionStyle.DEFAULT,
                isEnabled = true,
                onClick = { onEvent(LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout) },
            ),
            onEvent = onEvent,
        )

        is LogoutUiState.PendingChangesError -> LogoutConfirmationDialog(
            title = stringResource(Res.string.logout_pending_changes_error_title),
            text = stringResource(
                Res.string.logout_pending_changes_error_message,
                stringResource(uiState.pendingResource),
            ),
            confirmAction = AppAlertDialogAction(
                text = stringResource(Res.string.logout_sign_out_anyway),
                style = AppAlertDialogActionStyle.DESTRUCTIVE,
                isEnabled = true,
                onClick = { onEvent(LogoutUiEvent.ConfirmLogoutClick.OnForceLogout) },
            ),
            onEvent = onEvent,
        )

        is LogoutUiState.Loading -> AppProgressDialog(
            message = stringResource(
                when (uiState.phase) {
                    LogoutPhase.SYNCING -> Res.string.logout_syncing_progress
                    LogoutPhase.ENDING_SESSION -> Res.string.logout_ending_session
                },
            ),
        )
    }
}

@Composable
private fun LogoutConfirmationDialog(
    title: String,
    text: String,
    confirmAction: AppAlertDialogAction,
    onEvent: (LogoutUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = title,
        text = text,
        actions = listOf(
            confirmAction,
            AppAlertDialogAction(
                text = stringResource(Res.string.logout_cancel),
                style = AppAlertDialogActionStyle.CANCEL,
                isEnabled = true,
                onClick = { onEvent(LogoutUiEvent.OnCancel) },
            ),
        ),
        onDismissRequest = { onEvent(LogoutUiEvent.OnDismiss) },
    )
}
