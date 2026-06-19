package com.quare.bibleplanner.core.provider.analytics

import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService

class DesktopAnalyticsService : AnalyticsService {
    override fun setUserProperty(
        name: String,
        value: String?,
    ) = Unit
}
