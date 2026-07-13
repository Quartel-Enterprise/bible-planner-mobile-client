package com.quare.bibleplanner.feature.donation.presentation

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface DonationUiEvent : UiEvent {
    data object Dismiss : DonationUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DONATION_DISMISSED,
            params = emptyMap(),
        )
    }

    data class Copy(
        val type: DonationType,
    ) : DonationUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.DONATION_METHOD_COPIED,
        )
    }

    data object OpenGitHubSponsors : DonationUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.GITHUB_SPONSORS_OPENED,
            params = emptyMap(),
        )
    }

    data object ToggleBitcoin : DonationUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.DONATION_SECTION_TOGGLED,
        )
    }

    data object ToggleUsdt : DonationUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.DONATION_SECTION_TOGGLED,
        )
    }

    data object TogglePix : DonationUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.DONATION_SECTION_TOGGLED,
        )
    }

    data object OpenPixQr : DonationUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PIX_QR_OPENED,
            params = emptyMap(),
        )
    }
}
