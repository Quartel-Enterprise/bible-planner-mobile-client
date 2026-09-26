package com.quare.bibleplanner.core.provider.analytics.fake

import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService

internal class RecordingAnalyticsService(
    private val appInstanceId: String?,
) : AnalyticsService {
    val loggedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    override fun setUserProperty(
        name: String,
        value: String?,
    ) = error("unused")

    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        loggedEvents += name to params
    }

    override suspend fun getAppInstanceId(): String? = appInstanceId
}
