package com.quare.bibleplanner.feature.accountdetails.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface RenameDeviceUiEvent : UiEvent {
    data class OnConfirmClick(
        val newName: String,
    ) : RenameDeviceUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.DEVICE_RENAMED,
        )
    }

    data object OnDismiss : RenameDeviceUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DEVICE_RENAME_CANCELLED,
            params = emptyMap(),
        )
    }
}
