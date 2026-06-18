package com.quare.bibleplanner.feature.loginsyncnudge.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import bibleplanner.feature.login_sync_nudge.generated.resources.Res
import bibleplanner.feature.login_sync_nudge.generated.resources.login_sync_nudge_dont_show_again
import bibleplanner.feature.login_sync_nudge.generated.resources.login_sync_nudge_login
import bibleplanner.feature.login_sync_nudge.generated.resources.login_sync_nudge_message
import bibleplanner.feature.login_sync_nudge.generated.resources.login_sync_nudge_not_now
import bibleplanner.feature.login_sync_nudge.generated.resources.login_sync_nudge_title
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.model.LoginSyncNudgeUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginSyncNudgeDialog(
    isDontShowAgainChecked: Boolean,
    onEvent: (LoginSyncNudgeUiEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(LoginSyncNudgeUiEvent.OnDismiss) },
        title = {
            Text(
                text = stringResource(Res.string.login_sync_nudge_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.login_sync_nudge_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    modifier = Modifier
                        .toggleable(
                            value = isDontShowAgainChecked,
                            role = Role.Checkbox,
                            onValueChange = { onEvent(LoginSyncNudgeUiEvent.OnDontShowAgainToggled(it)) },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = isDontShowAgainChecked,
                        onCheckedChange = null,
                    )
                    Text(
                        text = stringResource(Res.string.login_sync_nudge_dont_show_again),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onEvent(LoginSyncNudgeUiEvent.OnLoginClick) }) {
                Text(text = stringResource(Res.string.login_sync_nudge_login))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(LoginSyncNudgeUiEvent.OnNotNow) }) {
                Text(text = stringResource(Res.string.login_sync_nudge_not_now))
            }
        },
    )
}
