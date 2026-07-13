package com.quare.bibleplanner.ui.utils.presentation

import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics

interface UiEvent {
    val analytics: EventAnalytics
}
