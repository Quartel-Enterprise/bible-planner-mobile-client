package com.quare.bibleplanner.core.provider.analytics.domain.service

interface AnalyticsService {
    fun setUserProperty(
        name: String,
        value: String?,
    )

    fun logEvent(
        name: String,
        params: Map<String, Any>,
    )
}
