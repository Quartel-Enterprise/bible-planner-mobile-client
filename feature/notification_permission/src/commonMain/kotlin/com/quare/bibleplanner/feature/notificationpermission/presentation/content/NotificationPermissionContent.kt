package com.quare.bibleplanner.feature.notificationpermission.presentation.content

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import bibleplanner.feature.notification_permission.generated.resources.Res
import bibleplanner.feature.notification_permission.generated.resources.notification_permission_decline
import bibleplanner.feature.notification_permission.generated.resources.notification_permission_title
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiEvent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NotificationPermissionContent(
    uiState: NotificationPermissionUiState,
    onEvent: (NotificationPermissionUiEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(NotificationPermissionUiEvent.OnDecline) },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
            )
        },
        title = {
            Text(text = stringResource(Res.string.notification_permission_title))
        },
        text = {
            Text(text = stringResource(uiState.textRes))
        },
        confirmButton = {
            TextButton(onClick = { onEvent(NotificationPermissionUiEvent.OnConfirm) }) {
                Text(text = stringResource(uiState.confirmButtonTextRes))
            }
        },
        dismissButton = if (uiState.shouldShowDismiss) {
            {
                TextButton(onClick = { onEvent(NotificationPermissionUiEvent.OnDecline) }) {
                    Text(text = stringResource(Res.string.notification_permission_decline))
                }
            }
        } else {
            null
        },
    )
}
