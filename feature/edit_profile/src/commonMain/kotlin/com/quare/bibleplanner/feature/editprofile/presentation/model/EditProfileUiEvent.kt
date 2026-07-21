package com.quare.bibleplanner.feature.editprofile.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface EditProfileUiEvent : UiEvent {
    data object OnChangeNameClick : EditProfileUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.EDIT_PROFILE_NAME_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnChangeImageClick : EditProfileUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.EDIT_PROFILE_PHOTO_CLICKED,
            params = emptyMap(),
        )
    }
}
