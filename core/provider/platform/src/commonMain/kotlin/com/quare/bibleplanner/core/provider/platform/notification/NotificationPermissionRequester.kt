package com.quare.bibleplanner.core.provider.platform.notification

interface NotificationPermissionRequester {
    suspend fun canPrompt(): Boolean

    suspend fun request(): NotificationPermissionPromptResult
}
