package com.quare.bibleplanner.core.provider.analytics

import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService

internal class WebAnalyticsService : AnalyticsService {
    override fun setUserProperty(
        name: String,
        value: String?,
    ) {}

    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {}

    override suspend fun getAppInstanceId(): String? = null
}
