package com.quare.bibleplanner.feature.notificationpermission.presentation.model

// Why: the texts follow isFirstTime (ask first, then point to the settings), so each UI picks its own copy.
data class NotificationPermissionUiState(
    val isFirstTime: Boolean,
    val shouldShowDismiss: Boolean,
)
