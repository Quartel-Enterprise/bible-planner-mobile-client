package com.quare.bibleplanner.feature.notificationpermission.presentation.model

/*
 * Why: the texts and the dismiss button follow isFirstTime (ask first, then point to the settings), so each UI
 * derives them.
 */
data class NotificationPermissionUiState(
    val isFirstTime: Boolean,
)
