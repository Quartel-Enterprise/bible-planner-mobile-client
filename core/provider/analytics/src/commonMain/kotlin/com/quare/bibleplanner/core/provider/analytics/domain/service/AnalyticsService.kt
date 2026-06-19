package com.quare.bibleplanner.core.provider.analytics.domain.service

interface AnalyticsService {
    fun setUserProperty(
        name: String,
        value: String?,
    )
}
