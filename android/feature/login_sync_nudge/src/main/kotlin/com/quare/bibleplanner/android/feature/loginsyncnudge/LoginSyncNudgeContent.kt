package com.quare.bibleplanner.android.feature.loginsyncnudge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.feature.loginsyncnudge.presentation.model.LoginSyncNudgeUiEvent

private val contentSpacing = 8.dp

@Composable
internal fun LoginSyncNudgeContent(
    isDontShowAgainChecked: Boolean,
    onEvent: (LoginSyncNudgeUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = stringResource(R.string.login_sync_nudge_title),
        confirmText = stringResource(R.string.login_sync_nudge_login),
        onConfirm = { onEvent(LoginSyncNudgeUiEvent.OnLoginClick) },
        dismissText = stringResource(R.string.login_sync_nudge_not_now),
        onDismiss = { onEvent(LoginSyncNudgeUiEvent.OnNotNow) },
        onDismissRequest = { onEvent(LoginSyncNudgeUiEvent.OnDismiss) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(contentSpacing)) {
                Text(text = stringResource(R.string.login_sync_nudge_message))
                Row(
                    modifier = Modifier.toggleable(
                        value = isDontShowAgainChecked,
                        role = Role.Checkbox,
                        onValueChange = { isChecked ->
                            onEvent(LoginSyncNudgeUiEvent.OnDontShowAgainToggled(isChecked))
                        },
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(contentSpacing),
                ) {
                    Checkbox(
                        checked = isDontShowAgainChecked,
                        onCheckedChange = null,
                    )
                    Text(
                        text = stringResource(R.string.login_sync_nudge_dont_show_again),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
    )
}
