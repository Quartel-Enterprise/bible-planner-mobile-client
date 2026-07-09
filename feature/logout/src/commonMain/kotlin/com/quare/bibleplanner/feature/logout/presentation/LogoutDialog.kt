package com.quare.bibleplanner.feature.logout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LogoutDialog(
    uiState: LogoutUiState,
    onEvent: (LogoutUiEvent) -> Unit,
) {
    val isLoading = uiState is LogoutUiState.Loading
    AlertDialog(
        onDismissRequest = {
            onEvent(LogoutUiEvent.OnDismiss)
        },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
        ),
        title = if (uiState is LogoutUiState.Loading) {
            null
        } else {
            {
                Text(
                    text = stringResource(
                        if (uiState is LogoutUiState.PendingChangesError) {
                            Res.string.logout_pending_changes_error_title
                        } else {
                            Res.string.logout_title
                        },
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        },
        text = {
            if (uiState is LogoutUiState.Loading) {
                LogoutProgressContent(phase = uiState.phase)
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = if (uiState is LogoutUiState.PendingChangesError) {
                            stringResource(
                                Res.string.logout_pending_changes_error_message,
                                stringResource(uiState.pendingResource),
                            )
                        } else {
                            stringResource(Res.string.logout_message)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                    )
                }
            }
        },
        confirmButton = {
            when (uiState) {
                is LogoutUiState.Loading -> {
                    Unit
                }

                LogoutUiState.Idle -> {
                    TextButton(
                        onClick = {
                            onEvent(LogoutUiEvent.ConfirmLogoutClick.OnConfirmLogout)
                        },
                    ) {
                        Text(text = stringResource(Res.string.logout_confirm))
                    }
                }

                is LogoutUiState.PendingChangesError -> {
                    TextButton(
                        onClick = {
                            onEvent(LogoutUiEvent.ConfirmLogoutClick.OnForceLogout)
                        },
                    ) {
                        Text(text = stringResource(Res.string.logout_sign_out_anyway))
                    }
                }
            }
        },
        dismissButton = {
            if (!isLoading) {
                TextButton(
                    onClick = {
                        onEvent(LogoutUiEvent.OnCancel)
                    },
                ) {
                    Text(text = stringResource(Res.string.logout_cancel))
                }
            }
        },
    )
}

@Composable
private fun LogoutProgressContent(phase: LogoutPhase) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(
                when (phase) {
                    LogoutPhase.SYNCING -> Res.string.logout_syncing_progress
                    LogoutPhase.ENDING_SESSION -> Res.string.logout_ending_session
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
