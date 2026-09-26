package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionPromptResult
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionRequester

internal class RecordingNotificationPermissionRequester : NotificationPermissionRequester {
    var requestCount: Int = 0
        private set

    override suspend fun canPrompt(): Boolean = true

    override suspend fun request(): NotificationPermissionPromptResult {
        requestCount++
        return NotificationPermissionPromptResult.GRANTED
    }
}
