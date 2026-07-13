package com.quare.bibleplanner.feature.contactsupport.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface ContactSupportUiEvent : UiEvent {
    data object OnDismiss : ContactSupportUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CONTACT_SUPPORT_DISMISSED,
            params = emptyMap(),
        )
    }

    data object OnSendEmailClick : ContactSupportUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.CONTACT_SUPPORT_EMAIL_OPENED,
        )
    }

    data object OnCopyEmailClick : ContactSupportUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CONTACT_SUPPORT_EMAIL_COPIED,
            params = emptyMap(),
        )
    }
}
