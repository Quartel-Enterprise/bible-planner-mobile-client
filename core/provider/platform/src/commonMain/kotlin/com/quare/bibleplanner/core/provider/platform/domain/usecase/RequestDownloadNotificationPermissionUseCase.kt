package com.quare.bibleplanner.core.provider.platform.domain.usecase

import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionPromptResult
import com.quare.bibleplanner.core.provider.platform.notification.NotificationPermissionRequester

class RequestDownloadNotificationPermissionUseCase(
    private val notificationPermissionRequester: NotificationPermissionRequester,
    private val trackEvent: TrackEvent,
    private val navigator: Navigator,
) : RequestDownloadNotificationPermission {
    override suspend fun invoke() {
        if (!notificationPermissionRequester.canPrompt()) return
        trackEvent(
            name = AnalyticsEventNames.NOTIFICATION_PERMISSION_PROMPTED,
            params = mapOf(AnalyticsParams.IS_FIRST_TIME to true),
        )
        val result = notificationPermissionRequester.request()
        trackEvent(
            name = AnalyticsEventNames.NOTIFICATION_PERMISSION_RESULT,
            params = mapOf(
                AnalyticsParams.IS_GRANTED to (result == NotificationPermissionPromptResult.GRANTED),
                AnalyticsParams.CAN_ASK_AGAIN to (result != NotificationPermissionPromptResult.PERMANENTLY_DENIED),
            ),
        )
        if (result == NotificationPermissionPromptResult.PERMANENTLY_DENIED) {
            navigator.navigate(NotificationPermissionNavRoute)
        }
    }
}
