package com.quare.bibleplanner.android.feature.notificationpermission

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.android.designsystem.dialog.AppAlertDialog
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiEvent
import com.quare.bibleplanner.feature.notificationpermission.presentation.model.NotificationPermissionUiState

@Composable
internal fun NotificationPermissionContent(
    uiState: NotificationPermissionUiState,
    onEvent: (NotificationPermissionUiEvent) -> Unit,
) {
    val declineText = stringResource(R.string.notification_permission_decline)
    val (textRes, confirmTextRes) = if (uiState.isFirstTime) {
        R.string.notification_permission_explanation to R.string.notification_permission_accept
    } else {
        R.string.notification_permission_settings_message to R.string.notification_permission_open_settings
    }
    AppAlertDialog(
        title = stringResource(R.string.notification_permission_title),
        text = stringResource(textRes),
        confirmText = stringResource(confirmTextRes),
        onConfirm = { onEvent(NotificationPermissionUiEvent.OnConfirm) },
        dismissText = declineText.takeIf { uiState.isFirstTime },
        onDismiss = { onEvent(NotificationPermissionUiEvent.OnDecline) },
        icon = Icons.Outlined.Notifications,
    )
}
