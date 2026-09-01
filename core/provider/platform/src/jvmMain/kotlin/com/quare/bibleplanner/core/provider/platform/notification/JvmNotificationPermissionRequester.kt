package com.quare.bibleplanner.core.provider.platform.notification

internal class JvmNotificationPermissionRequester : NotificationPermissionRequester {
    override suspend fun canPrompt(): Boolean = false

    override suspend fun request(): NotificationPermissionPromptResult = NotificationPermissionPromptResult.DENIED
}
