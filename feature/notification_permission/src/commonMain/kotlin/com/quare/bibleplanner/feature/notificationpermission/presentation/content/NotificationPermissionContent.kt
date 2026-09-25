package com.quare.bibleplanner.feature.notificationpermission.presentation.content

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import bibleplanner.feature.notification_permission.generated.resources.Res
import bibleplanner.feature.notification_permission.generated.resources.notification_permission_decline
import bibleplanner.feature.notification_permission.generated.resources.notification_permission_title
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiEvent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiState
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NotificationPermissionContent(
    uiState: NotificationPermissionUiState,
    onEvent: (NotificationPermissionUiEvent) -> Unit,
) {
    val declineText = stringResource(Res.string.notification_permission_decline)
    AppAlertDialog(
        title = stringResource(Res.string.notification_permission_title),
        text = stringResource(uiState.textRes),
        confirmText = stringResource(uiState.confirmButtonTextRes),
        onConfirm = { onEvent(NotificationPermissionUiEvent.OnConfirm) },
        dismissText = declineText.takeIf { uiState.shouldShowDismiss },
        onDismiss = { onEvent(NotificationPermissionUiEvent.OnDecline) },
        icon = Icons.Outlined.Notifications,
    )
}
