package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionPromptResult
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionRequester
import kotlinx.coroutines.CompletableDeferred

internal class RecordingNotificationPermissionRequester : NotificationPermissionRequester {
    var requestCount: Int = 0
        private set

    // Why: lets a test hold the permission prompt open to act while an update is still running.
    var pendingAnswer: CompletableDeferred<Unit>? = null

    override suspend fun canPrompt(): Boolean = true

    override suspend fun request(): NotificationPermissionPromptResult {
        requestCount++
        pendingAnswer?.await()
        return NotificationPermissionPromptResult.GRANTED
    }
}
