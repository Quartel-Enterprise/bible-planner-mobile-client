package com.quare.bibleplanner.feature.editprofile.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent
import io.github.vinceglb.filekit.PlatformFile

internal sealed interface ProfilePhotoUiEvent : UiEvent {
    data object OnUseProviderPhotoClick : ProfilePhotoUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PROFILE_PHOTO_SOURCE_SELECTED,
            params = mapOf(AnalyticsParams.SOURCE to "provider"),
        )
    }

    data object OnPickFromGalleryClick : ProfilePhotoUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PROFILE_PHOTO_SOURCE_SELECTED,
            params = mapOf(AnalyticsParams.SOURCE to "gallery"),
        )
    }

    data object OnTakePhotoClick : ProfilePhotoUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PROFILE_PHOTO_SOURCE_SELECTED,
            params = mapOf(AnalyticsParams.SOURCE to "camera"),
        )
    }

    data object OnRemovePhotoClick : ProfilePhotoUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PROFILE_PHOTO_REMOVED,
            params = emptyMap(),
        )
    }

    data class OnImagePicked(
        val file: PlatformFile?,
    ) : ProfilePhotoUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }
}
