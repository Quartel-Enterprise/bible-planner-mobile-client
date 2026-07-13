package com.quare.bibleplanner.feature.accountdetails.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface AccountDetailsUiEvent : UiEvent {
    data object OnToggleDevices : AccountDetailsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.CONNECTED_DEVICES_TOGGLED,
        )
    }

    data class OnRenameDeviceClick(
        val device: DeviceUiModel,
    ) : AccountDetailsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.RENAME_DEVICE_CLICKED,
            params = emptyMap(),
        )
    }

    data class OnSignOutDeviceClick(
        val device: DeviceUiModel,
    ) : AccountDetailsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.DEVICE_SIGNED_OUT,
        )
    }

    data object OnLogoutClick : AccountDetailsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.LOGOUT_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to "account_details"),
        )
    }
}
