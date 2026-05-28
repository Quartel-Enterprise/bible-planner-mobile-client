package com.quare.bibleplanner.feature.logout.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import bibleplanner.feature.logout.generated.resources.Res
import bibleplanner.feature.logout.generated.resources.logout_cancel
import bibleplanner.feature.logout.generated.resources.logout_confirm
import bibleplanner.feature.logout.generated.resources.logout_message
import bibleplanner.feature.logout.generated.resources.logout_title
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiEvent
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LogoutDialog(
    uiState: LogoutUiState,
    onEvent: (LogoutUiEvent) -> Unit,
) {
    val isLoading = uiState == LogoutUiState.Loading
    AlertDialog(
        onDismissRequest = {
            onEvent(LogoutUiEvent.OnDismiss)
        },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
        ),
        title = {
            Text(
                text = stringResource(Res.string.logout_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.logout_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )
            }
        },
        confirmButton = {
            when (uiState) {
                LogoutUiState.Loading -> {
                    CircularProgressIndicator()
                }

                LogoutUiState.Idle -> {
                    TextButton(
                        onClick = {
                            onEvent(LogoutUiEvent.OnConfirmLogout)
                        },
                    ) {
                        Text(text = stringResource(Res.string.logout_confirm))
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
