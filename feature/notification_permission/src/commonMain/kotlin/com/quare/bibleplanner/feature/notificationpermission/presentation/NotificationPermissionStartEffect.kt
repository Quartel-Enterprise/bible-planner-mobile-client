package com.quare.bibleplanner.feature.notificationpermission.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

@Composable
expect fun NotificationPermissionStartEffect(onNavigate: (NavKey) -> Unit)
