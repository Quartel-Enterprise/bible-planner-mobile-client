package com.quare.bibleplanner.core.provider.platform.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

internal class IosNotificationPermissionRequester : NotificationPermissionRequester {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun canPrompt(): Boolean = suspendCancellableCoroutine { continuation ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            continuation.resume(settings?.authorizationStatus == UNAuthorizationStatusNotDetermined)
        }
    }

    override suspend fun request(): NotificationPermissionPromptResult = suspendCancellableCoroutine { continuation ->
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { isGranted, _ ->
            val result = if (isGranted) {
                NotificationPermissionPromptResult.GRANTED
            } else {
                NotificationPermissionPromptResult.DENIED
            }
            continuation.resume(result)
        }
    }
}
