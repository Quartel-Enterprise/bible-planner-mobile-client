package com.quare.bibleplanner.feature.notificationpermission.presentation.model

sealed interface NotificationPermissionUiEvent {
    data object OnConfirm : NotificationPermissionUiEvent
    data object OnDecline : NotificationPermissionUiEvent
    data class OnPermissionResult(
        val granted: Boolean,
        val canAskAgain: Boolean,
    ) : NotificationPermissionUiEvent
}
