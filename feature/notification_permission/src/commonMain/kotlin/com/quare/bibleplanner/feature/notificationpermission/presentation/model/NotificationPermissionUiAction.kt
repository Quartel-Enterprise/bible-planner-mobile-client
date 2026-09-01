package com.quare.bibleplanner.feature.notificationpermission.presentation.model

sealed interface NotificationPermissionUiAction {
    data object RequestSystemPermission : NotificationPermissionUiAction

    data object OpenNotificationSettings : NotificationPermissionUiAction
}
