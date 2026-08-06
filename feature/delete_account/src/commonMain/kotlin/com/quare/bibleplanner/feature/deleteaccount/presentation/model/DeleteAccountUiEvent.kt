package com.quare.bibleplanner.feature.deleteaccount.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface DeleteAccountUiEvent : UiEvent {
    data class OnConfirmationTextChange(
        val text: String,
    ) : DeleteAccountUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnConfirmDelete : DeleteAccountUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.ACCOUNT_DELETE_CONFIRMED,
                AnalyticsEventNames.ACCOUNT_DELETE_FAILED,
            ),
        )
    }

    data object OnCancel : DeleteAccountUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ACCOUNT_DELETE_CANCELLED,
            params = emptyMap(),
        )
    }

    data object OnDismiss : DeleteAccountUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ACCOUNT_DELETE_CANCELLED,
            params = emptyMap(),
        )
    }
}
