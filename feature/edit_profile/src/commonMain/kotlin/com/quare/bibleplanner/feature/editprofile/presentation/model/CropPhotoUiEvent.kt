package com.quare.bibleplanner.feature.editprofile.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface CropPhotoUiEvent : UiEvent {
    data class OnViewportMeasured(
        val areaWidth: Float,
        val areaHeight: Float,
        val circleDiameter: Float,
    ) : CropPhotoUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data class OnTransform(
        val panX: Float,
        val panY: Float,
        val zoomChange: Float,
    ) : CropPhotoUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data class OnZoomChanged(
        val zoom: Float,
    ) : CropPhotoUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnCancelClick : CropPhotoUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PROFILE_PHOTO_CROP_CANCELLED,
            params = emptyMap(),
        )
    }

    data object OnConfirmClick : CropPhotoUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PROFILE_PHOTO_UPDATED,
            params = emptyMap(),
        )
    }
}
