package com.quare.bibleplanner.feature.notificationpermission.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface NotificationPermissionUiEvent : UiEvent {
    data object OnConfirm : NotificationPermissionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.NOTIFICATION_PERMISSION_PROMPTED,
        )
    }

    data object OnDecline : NotificationPermissionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.NOTIFICATION_PERMISSION_DECLINED,
            params = emptyMap(),
        )
    }

    data class OnPermissionResult(
        val granted: Boolean,
        val canAskAgain: Boolean,
    ) : NotificationPermissionUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.NOTIFICATION_PERMISSION_RESULT,
        )
    }
}
