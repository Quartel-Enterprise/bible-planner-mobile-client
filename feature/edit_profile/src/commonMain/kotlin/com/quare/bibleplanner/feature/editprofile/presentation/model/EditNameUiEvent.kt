package com.quare.bibleplanner.feature.editprofile.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface EditNameUiEvent : UiEvent {
    data class OnSaveClick(
        val name: String,
    ) : EditNameUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PROFILE_NAME_UPDATED,
            params = emptyMap(),
        )
    }
}
