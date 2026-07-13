package com.quare.bibleplanner.ui.utils.presentation

import androidx.lifecycle.ViewModel
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent

abstract class TrackedViewModel<E : UiEvent>(
    protected val trackEvent: TrackEvent,
) : ViewModel() {
    fun onEvent(event: E) {
        val analytics = event.analytics
        if (analytics is EventAnalytics.Track.Automatic) {
            trackEvent(
                name = analytics.name,
                params = analytics.params,
            )
        }
        handleEvent(event)
    }

    protected abstract fun handleEvent(event: E)
}
