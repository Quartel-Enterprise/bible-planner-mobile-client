package com.quare.bibleplanner.feature.notificationpermission.presentation.model

import org.jetbrains.compose.resources.StringResource

data class NotificationPermissionUiState(
    val isFirstTime: Boolean,
    val textRes: StringResource,
    val confirmButtonTextRes: StringResource,
    val shouldShowDismiss: Boolean,
)
