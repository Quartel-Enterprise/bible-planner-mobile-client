package com.quare.bibleplanner.e2e.harness

import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService

internal class NoOpAnalyticsService : AnalyticsService {
    override fun setUserProperty(
        name: String,
        value: String?,
    ) = Unit

    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) = Unit

    override suspend fun getAppInstanceId(): String? = null
}
